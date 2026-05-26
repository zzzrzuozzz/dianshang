package com.dianshang.admin.ops.advertisement.service;

import com.dianshang.admin.common.BusinessException;
import com.dianshang.admin.common.Jsons;
import com.dianshang.admin.member.repository.MemberRepository;
import com.dianshang.admin.ops.advertisement.dto.*;
import com.dianshang.admin.ops.advertisement.entity.OpsAdvertisementEntity;
import com.dianshang.admin.ops.advertisement.repository.OpsAdvertisementRepository;
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
public class AdvertisementService {

    private static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("yyyy-M-d HH:mm");
    private static final DateTimeFormatter INPUT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Map<String, String> ADV_TYPE_LABEL = Map.of(
            "carousel", "APP首页轮播",
            "activity", "APP首页活动",
            "guess", "猜你喜欢"
    );

    private final OpsAdvertisementRepository advertisementRepository;
    private final MemberRepository memberRepository;

    public AdvertisementService(OpsAdvertisementRepository advertisementRepository,
                                MemberRepository memberRepository) {
        this.advertisementRepository = advertisementRepository;
        this.memberRepository = memberRepository;
    }

    public AdvertisementPageVO list(String tab, String title, LocalDate startDate, LocalDate endDate,
                                    int page, int pageSize) {
        Specification<OpsAdvertisementEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));
            if (StringUtils.hasText(title)) {
                String kw = "%" + title.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), kw),
                        cb.like(cb.lower(root.get("advCode")), kw)
                ));
            }
            if ("online".equals(tab)) {
                predicates.add(cb.isTrue(root.get("online")));
            } else if ("offline".equals(tab)) {
                predicates.add(cb.isFalse(root.get("online")));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startTime"), startDate.atStartOfDay()));
            }
            if (endDate != null) {
                predicates.add(cb.lessThan(root.get("startTime"), endDate.plusDays(1).atStartOfDay()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<OpsAdvertisementEntity> result = advertisementRepository.findAll(
                spec,
                PageRequest.of(Math.max(page - 1, 0), pageSize,
                        Sort.by(Sort.Direction.ASC, "sortNum").and(Sort.by(Sort.Direction.DESC, "createdAt")))
        );

        AdvertisementPageVO vo = new AdvertisementPageVO();
        vo.setList(result.getContent().stream().map(this::toListVO).toList());
        vo.setTotal(result.getTotalElements());
        vo.setPage(page);
        vo.setPageSize(pageSize);
        vo.setTotalPages(result.getTotalPages() == 0 ? 1 : result.getTotalPages());
        return vo;
    }

    public AdvertisementDetailVO detail(String advCode) {
        return toDetailVO(require(advCode));
    }

    @Transactional
    public String save(AdvertisementSaveRequest request) {
        OpsAdvertisementEntity entity;
        if (StringUtils.hasText(request.getAdvCode())) {
            entity = require(request.getAdvCode());
        } else {
            entity = new OpsAdvertisementEntity();
            entity.setAdvCode(nextCode());
            entity.setCreatedAt(LocalDateTime.now());
            entity.setExposureCount(0);
            entity.setClickCount(0);
        }

        entity.setAdvType(request.getCategory());
        entity.setTitle(request.getTitle().trim());
        entity.setIntro(request.getIntro());
        entity.setJumpType(request.getJumpType());
        entity.setJumpUrl(request.getJumpUrl());
        entity.setDetailHtml(request.getDetail());
        entity.setCoverImagesJson(Jsons.toJson(request.getCoverImages()));
        entity.setStartTime(parseDateTime(request.getStartTime()));
        entity.setEndTime(parseDateTime(request.getEndTime()));
        entity.setOnline(request.getOnline() == null || request.getOnline());
        entity.setAppPush(Boolean.TRUE.equals(request.getAppPush()));
        entity.setAudienceJson(OpsAudienceSupport.buildAudienceJson(
                request.getMemberLevels(),
                request.getRegions(),
                request.getTags(),
                null
        ));

        if (entity.getSortNum() == null) {
            entity.setSortNum((int) advertisementRepository.countByDeletedFalse() + 1);
        }

        advertisementRepository.save(entity);
        return entity.getAdvCode();
    }

    @Transactional
    public void toggleOnline(String advCode, boolean online) {
        OpsAdvertisementEntity entity = require(advCode);
        entity.setOnline(online);
        advertisementRepository.save(entity);
    }

    @Transactional
    public void pin(String advCode) {
        OpsAdvertisementEntity entity = require(advCode);
        int minSort = advertisementRepository.findAll().stream()
                .filter(a -> !Boolean.TRUE.equals(a.getDeleted()))
                .mapToInt(a -> a.getSortNum() == null ? 0 : a.getSortNum())
                .min()
                .orElse(0);
        entity.setSortNum(minSort - 1);
        advertisementRepository.save(entity);
    }

    @Transactional
    public void delete(String advCode) {
        OpsAdvertisementEntity entity = require(advCode);
        entity.setDeleted(true);
        advertisementRepository.save(entity);
    }

    private AdvertisementListVO toListVO(OpsAdvertisementEntity e) {
        AdvertisementListVO vo = new AdvertisementListVO();
        vo.setId(e.getAdvCode());
        vo.setName(e.getTitle());
        vo.setPublishTime(e.getStartTime() == null ? "-" : e.getStartTime().format(DISPLAY));
        vo.setEndTime(e.getEndTime() == null ? "-" : e.getEndTime().format(DISPLAY));
        vo.setAdvType(ADV_TYPE_LABEL.getOrDefault(e.getAdvType(), e.getAdvType()));
        vo.setExposureCount(e.getExposureCount());
        vo.setClickCount(e.getClickCount());
        vo.setSort(e.getSortNum());
        vo.setStatus(Boolean.TRUE.equals(e.getOnline()) ? 1 : 0);
        vo.setStatusText(Boolean.TRUE.equals(e.getOnline()) ? "已上线" : "已下架");
        return vo;
    }

    private AdvertisementDetailVO toDetailVO(OpsAdvertisementEntity e) {
        AdvertisementDetailVO vo = new AdvertisementDetailVO();
        vo.setAdvCode(e.getAdvCode());
        vo.setCategory(e.getAdvType());
        vo.setTitle(e.getTitle());
        vo.setIntro(e.getIntro());
        vo.setJumpType(e.getJumpType());
        vo.setJumpUrl(e.getJumpUrl());
        vo.setDetail(e.getDetailHtml());
        vo.setCoverImages(Jsons.toStringList(e.getCoverImagesJson()));
        vo.setOnline(e.getOnline());
        vo.setAppPush(e.getAppPush());
        if (e.getStartTime() != null) {
            vo.setStartTime(e.getStartTime().format(INPUT));
        }
        if (e.getEndTime() != null) {
            vo.setEndTime(e.getEndTime().format(INPUT));
        }
        Map<String, Object> audience = OpsAudienceSupport.parseAudience(e.getAudienceJson());
        List<String> levels = new ArrayList<>();
        List<List<String>> regions = new ArrayList<>();
        Map<String, List<String>> tags = new LinkedHashMap<>();
        List<String> pushMethod = new ArrayList<>();
        OpsAudienceSupport.fillAudienceFields(audience, levels, regions, tags, pushMethod);
        vo.setMemberLevels(levels.isEmpty() ? List.of("all") : levels);
        vo.setRegions(regions);
        vo.setTags(tags);
        vo.setEstimatedUsers(OpsAudienceSupport.estimateUsers(memberRepository, levels, regions, tags));
        return vo;
    }

    private OpsAdvertisementEntity require(String code) {
        return advertisementRepository.findByAdvCodeAndDeletedFalse(code)
                .orElseThrow(() -> new BusinessException("广告不存在"));
    }

    private String nextCode() {
        return "AD" + (6542 + advertisementRepository.countByDeletedFalse());
    }

    private LocalDateTime parseDateTime(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        return LocalDateTime.parse(text, INPUT);
    }
}
