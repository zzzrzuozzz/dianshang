package com.dianshang.admin.ops.notification.service;

import com.dianshang.admin.common.BusinessException;
import com.dianshang.admin.common.Jsons;
import com.dianshang.admin.member.repository.MemberRepository;
import com.dianshang.admin.ops.notification.dto.*;
import com.dianshang.admin.ops.notification.entity.OpsNotificationEntity;
import com.dianshang.admin.ops.notification.repository.OpsNotificationRepository;
import com.dianshang.admin.ops.support.OpsAudienceSupport;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class NotificationService {

    private static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("yyyy-M-d HH:mm");
    private static final DateTimeFormatter INPUT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Map<String, String> CATEGORY_TEXT = Map.of(
            "seckill", "秒杀活动",
            "service", "客服消息",
            "ops", "运营消息",
            "system", "系统通知"
    );

    private final OpsNotificationRepository notificationRepository;
    private final MemberRepository memberRepository;

    public NotificationService(OpsNotificationRepository notificationRepository,
                               MemberRepository memberRepository) {
        this.notificationRepository = notificationRepository;
        this.memberRepository = memberRepository;
    }

    public NotificationPageVO list(String msgType, String tab, String title,
                                   LocalDate startDate, LocalDate endDate,
                                   int page, int pageSize) {
        Specification<OpsNotificationEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("deleted"), Boolean.FALSE));
            predicates.add(cb.equal(root.get("msgType"), msgType));
            if (StringUtils.hasText(title)) {
                String kw = "%" + title.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), kw),
                        cb.like(cb.lower(root.get("notifyCode")), kw)
                ));
            }
            if ("published".equals(tab)) {
                predicates.add(cb.equal(root.get("publishStatus"), 1));
            } else if ("draft".equals(tab)) {
                predicates.add(cb.equal(root.get("publishStatus"), 0));
            } else if (StringUtils.hasText(tab) && !"all".equals(tab)) {
                predicates.add(cb.equal(root.get("msgCategory"), tab));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("publishedAt"), startDate.atStartOfDay()));
            }
            if (endDate != null) {
                predicates.add(cb.lessThan(root.get("publishedAt"), endDate.plusDays(1).atStartOfDay()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<OpsNotificationEntity> result = notificationRepository.findAll(
                spec,
                PageRequest.of(Math.max(page - 1, 0), pageSize, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        NotificationPageVO vo = new NotificationPageVO();
        vo.setList(result.getContent().stream().map(this::toListVO).toList());
        vo.setTotal(result.getTotalElements());
        vo.setPage(page);
        vo.setPageSize(pageSize);
        vo.setTotalPages(result.getTotalPages() == 0 ? 1 : result.getTotalPages());
        return vo;
    }

    public NotificationDetailVO detail(String notifyCode) {
        OpsNotificationEntity entity = require(notifyCode);
        return toDetailVO(entity);
    }

    @Transactional
    public String save(NotificationSaveRequest request) {
        validateSave(request);
        OpsNotificationEntity entity;
        if (StringUtils.hasText(request.getNotifyCode())) {
            entity = require(request.getNotifyCode());
        } else {
            entity = new OpsNotificationEntity();
            entity.setNotifyCode(nextNotifyCode());
            entity.setCreatedAt(LocalDateTime.now());
            initCounters(entity);
        }

        entity.setMsgType(request.getMsgType());
        entity.setMsgCategory(request.getCategory());
        entity.setJumpType(request.getJumpType());
        entity.setInnerLinkType(request.getInnerLinkType());
        entity.setJumpUrl(request.getJumpUrl());
        entity.setDetailHtml(request.getDetail());
        entity.setCoverImagesJson(Jsons.toJson(request.getCoverImages()));
        entity.setGenerateTypesJson(Jsons.toJson(request.getGenerateTypes()));
        entity.setSendType(request.getSendType() == null ? 1 : request.getSendType());
        entity.setScheduledTime(parseDateTime(request.getPublishTime()));
        entity.setAppPush(Boolean.TRUE.equals(request.getAppPush()));

        String audienceJson = OpsAudienceSupport.buildAudienceJson(
                request.getMemberLevels(),
                request.getRegions(),
                request.getTags(),
                request.getPushMethod()
        );
        entity.setAudienceJson(audienceJson);

        int estimate = request.getEstimatedUsers() != null
                ? request.getEstimatedUsers()
                : OpsAudienceSupport.estimateUsers(memberRepository,
                request.getMemberLevels(), request.getRegions(), request.getTags());
        entity.setEstimatedUsers(estimate);
        entity.setPushUserText(formatPushUser(estimate));

        applyContentFields(entity, request);

        boolean immediate = entity.getSendType() == 1;
        if (immediate) {
            if (entity.getPublishStatus() == null || entity.getPublishStatus() == 0) {
                publishEntity(entity, estimate);
            } else {
                entity.setEstimatedUsers(estimate);
                entity.setPushUserText(formatPushUser(estimate));
            }
        } else {
            entity.setPublishStatus(0);
        }

        notificationRepository.saveAndFlush(entity);
        return entity.getNotifyCode();
    }

    @Transactional
    public void resend(String notifyCode) {
        OpsNotificationEntity entity = require(notifyCode);
        initCounters(entity);
        int volume = entity.getEstimatedUsers() == null ? 0 : entity.getEstimatedUsers();
        if (entity.getPublishStatus() == null || entity.getPublishStatus() == 0) {
            publishEntity(entity, volume);
        } else {
            entity.setPushCount(entity.getPushCount() + 1);
            entity.setPushVolume(entity.getPushVolume() + volume);
            entity.setReceiveVolume(entity.getReceiveVolume() + volume);
            entity.setPublishedAt(LocalDateTime.now());
        }
        notificationRepository.save(entity);
    }

    @Transactional
    public void delete(String notifyCode) {
        OpsNotificationEntity entity = require(notifyCode);
        entity.setDeleted(true);
        notificationRepository.save(entity);
    }

    @Transactional
    public int batchDelete(List<String> codes) {
        int count = 0;
        for (String code : codes) {
            if (!StringUtils.hasText(code)) {
                continue;
            }
            var found = notificationRepository.findByNotifyCodeAndDeletedFalse(code.trim());
            if (found.isPresent()) {
                OpsNotificationEntity entity = found.get();
                entity.setDeleted(true);
                notificationRepository.save(entity);
                count++;
            }
        }
        return count;
    }

    @Transactional
    public int batchResend(List<String> codes) {
        int count = 0;
        for (String code : codes) {
            if (!StringUtils.hasText(code)) {
                continue;
            }
            try {
                resend(code.trim());
                count++;
            } catch (BusinessException ignored) {
                // 跳过不存在或已删除的记录
            }
        }
        return count;
    }

    public int estimate(AudienceEstimateRequest request) {
        return OpsAudienceSupport.estimateUsers(
                memberRepository,
                request.getMemberLevels(),
                request.getRegions(),
                request.getTags()
        );
    }

    private void validateSave(NotificationSaveRequest request) {
        if (!StringUtils.hasText(request.getMsgType())) {
            throw new BusinessException("消息类型不能为空");
        }
        String type = request.getMsgType().trim().toUpperCase();
        request.setMsgType(type);
        if ("SMS".equals(type)) {
            if (!StringUtils.hasText(request.getSmsContent())) {
                throw new BusinessException("请输入短信内容");
            }
        } else if ("STATION".equals(type)) {
            if (!StringUtils.hasText(request.getTitle())) {
                throw new BusinessException("请输入消息标题");
            }
            if (!StringUtils.hasText(request.getStationContent())) {
                throw new BusinessException("请输入消息内容");
            }
        } else {
            if (!StringUtils.hasText(request.getTitle())) {
                throw new BusinessException("请输入消息标题");
            }
            if (!StringUtils.hasText(request.getCategory())) {
                throw new BusinessException("请选择消息分类");
            }
        }
    }

    private void applyContentFields(OpsNotificationEntity entity, NotificationSaveRequest request) {
        if ("SMS".equals(request.getMsgType())) {
            String body = request.getSmsContent().trim();
            entity.setContent(body);
            entity.setTitle(body.length() > 60 ? body.substring(0, 60) : body);
            entity.setIntro(null);
        } else if ("STATION".equals(request.getMsgType())) {
            entity.setTitle(request.getTitle().trim());
            entity.setContent(request.getStationContent());
            entity.setIntro(null);
        } else {
            entity.setTitle(request.getTitle().trim());
            entity.setIntro(request.getIntro());
            entity.setContent(null);
        }
    }

    private void publishEntity(OpsNotificationEntity entity, int volume) {
        initCounters(entity);
        entity.setPublishStatus(1);
        entity.setPublishedAt(LocalDateTime.now());
        entity.setPushCount(entity.getPushCount() + 1);
        entity.setPushVolume(entity.getPushVolume() + volume);
        entity.setReceiveVolume(entity.getReceiveVolume() + volume);
        if (entity.getClickCount() == 0 && volume > 0) {
            entity.setClickCount(volume / 2);
        }
    }

    private NotificationListVO toListVO(OpsNotificationEntity e) {
        NotificationListVO vo = new NotificationListVO();
        vo.setId(e.getNotifyCode());
        vo.setTitle(e.getTitle());
        vo.setPublishStatus(e.getPublishStatus());
        vo.setPublishStatusText(e.getPublishStatus() != null && e.getPublishStatus() == 1 ? "已发布" : "未发布");
        vo.setAppPush(Boolean.TRUE.equals(e.getAppPush()) ? "已推送" : "不推送");
        LocalDateTime pub = e.getPublishedAt() != null ? e.getPublishedAt() : e.getCreatedAt();
        vo.setPublishTime(pub == null ? "-" : pub.format(DISPLAY));
        vo.setMsgCategory(e.getMsgCategory());
        vo.setMsgCategoryText(CATEGORY_TEXT.getOrDefault(e.getMsgCategory(), "-"));
        vo.setPushCount(e.getPushCount());
        vo.setPushVolume(e.getPushVolume());
        vo.setClickCount(e.getClickCount());
        vo.setReceiveVolume(e.getReceiveVolume());
        vo.setPushUser(e.getPushUserText() == null ? "-" : e.getPushUserText());
        return vo;
    }

    private NotificationDetailVO toDetailVO(OpsNotificationEntity e) {
        NotificationDetailVO vo = new NotificationDetailVO();
        vo.setNotifyCode(e.getNotifyCode());
        vo.setMsgType(e.getMsgType());
        vo.setCategory(e.getMsgCategory());
        vo.setTitle(e.getTitle());
        vo.setIntro(e.getIntro());
        vo.setJumpType(e.getJumpType());
        vo.setInnerLinkType(e.getInnerLinkType());
        vo.setJumpUrl(e.getJumpUrl());
        vo.setDetail(e.getDetailHtml());
        vo.setCoverImages(Jsons.toStringList(e.getCoverImagesJson()));
        vo.setSendType(e.getSendType());
        vo.setAppPush(e.getAppPush());
        vo.setGenerateTypes(Jsons.toStringList(e.getGenerateTypesJson()));
        vo.setEstimatedUsers(e.getEstimatedUsers());
        if (e.getScheduledTime() != null) {
            vo.setPublishTime(e.getScheduledTime().format(INPUT));
        }
        Map<String, Object> audience = OpsAudienceSupport.parseAudience(e.getAudienceJson());
        List<String> levels = new ArrayList<>();
        List<List<String>> regions = new ArrayList<>();
        Map<String, List<String>> tags = new LinkedHashMap<>();
        List<String> pushMethod = new ArrayList<>();
        OpsAudienceSupport.fillAudienceFields(audience, levels, regions, tags, pushMethod);
        vo.setMemberLevels(levels.isEmpty() ? List.of("all") : levels);
        vo.setRegions(regions);
        vo.setTags(tags.isEmpty() ? defaultTags() : tags);
        vo.setPushMethod(pushMethod.isEmpty() ? List.of("tag") : pushMethod);
        if ("SMS".equals(e.getMsgType())) {
            vo.setSmsContent(e.getContent());
        } else if ("STATION".equals(e.getMsgType())) {
            vo.setStationContent(e.getContent());
        }
        return vo;
    }

    private Map<String, List<String>> defaultTags() {
        Map<String, List<String>> tags = new LinkedHashMap<>();
        tags.put("newUser", List.of("all"));
        tags.put("firstBuy", List.of("all"));
        tags.put("repurchase", List.of("all"));
        tags.put("active", List.of("all"));
        return tags;
    }

    private OpsNotificationEntity require(String code) {
        return notificationRepository.findByNotifyCodeAndDeletedFalse(code)
                .orElseThrow(() -> new BusinessException("推送记录不存在"));
    }

    private void initCounters(OpsNotificationEntity entity) {
        if (entity.getPushCount() == null) {
            entity.setPushCount(0);
        }
        if (entity.getPushVolume() == null) {
            entity.setPushVolume(0);
        }
        if (entity.getClickCount() == null) {
            entity.setClickCount(0);
        }
        if (entity.getReceiveVolume() == null) {
            entity.setReceiveVolume(0);
        }
        if (entity.getPublishStatus() == null) {
            entity.setPublishStatus(0);
        }
        if (entity.getSendType() == null) {
            entity.setSendType(1);
        }
        if (entity.getAppPush() == null) {
            entity.setAppPush(false);
        }
        if (entity.getDeleted() == null) {
            entity.setDeleted(false);
        }
        if (entity.getEstimatedUsers() == null) {
            entity.setEstimatedUsers(0);
        }
    }

    private String nextNotifyCode() {
        long base = 6542 + notificationRepository.countByDeletedFalse();
        for (int i = 0; i < 100; i++) {
            String code = "NT" + (base + i);
            if (notificationRepository.findByNotifyCodeAndDeletedFalse(code).isEmpty()) {
                return code;
            }
        }
        return "NT" + System.currentTimeMillis();
    }

    private String formatPushUser(int estimate) {
        if (estimate >= 1000) {
            return (estimate / 100) * 100 + "位";
        }
        return estimate + "位";
    }

    private LocalDateTime parseDateTime(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        return LocalDateTime.parse(text, INPUT);
    }
}
