package com.dianshang.admin.member.service;

import com.dianshang.admin.common.BusinessException;
import com.dianshang.admin.common.Jsons;
import com.dianshang.admin.member.dto.*;
import com.dianshang.admin.member.entity.*;
import com.dianshang.admin.member.repository.*;
import com.dianshang.admin.member.support.MemberSpecifications;
import com.dianshang.admin.member.support.MemberSupport;
import com.dianshang.admin.member.support.TagRuleMatcher;
import com.dianshang.admin.order.entity.OrderEntity;
import com.dianshang.admin.order.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MemberService {

    private static final String CFG_RULES = "growth_rules";
    private static final String CFG_REWARDS = "growth_rewards";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final Map<String, String> ORDER_STATUS_TEXT = Map.of(
            "pending_payment", "待付款",
            "pending_ship", "待发货",
            "paid", "待发货",
            "shipped", "已发货",
            "completed", "已完成",
            "cancelled", "已取消"
    );

    private final MemberRepository memberRepository;
    private final MemberLevelRepository levelRepository;
    private final MemberTagRepository tagRepository;
    private final MemberTagRelRepository tagRelRepository;
    private final MemberAddressRepository addressRepository;
    private final GrowthTaskRepository taskRepository;
    private final MemberLedgerRepository ledgerRepository;
    private final GrowthConfigRepository configRepository;
    private final OrderRepository orderRepository;

    public MemberService(MemberRepository memberRepository,
                         MemberLevelRepository levelRepository,
                         MemberTagRepository tagRepository,
                         MemberTagRelRepository tagRelRepository,
                         MemberAddressRepository addressRepository,
                         GrowthTaskRepository taskRepository,
                         MemberLedgerRepository ledgerRepository,
                         GrowthConfigRepository configRepository,
                         OrderRepository orderRepository) {
        this.memberRepository = memberRepository;
        this.levelRepository = levelRepository;
        this.tagRepository = tagRepository;
        this.tagRelRepository = tagRelRepository;
        this.addressRepository = addressRepository;
        this.taskRepository = taskRepository;
        this.ledgerRepository = ledgerRepository;
        this.configRepository = configRepository;
        this.orderRepository = orderRepository;
    }

    public MemberPageVO list(String account, String nickname, String tab, String levelTab,
                             LocalDate startDate, LocalDate endDate, int page, int pageSize) {
        return pageMembers(account, nickname, tab, levelTab, startDate, endDate, page, pageSize);
    }

    public MemberPageVO growthList(String account, String nickname, String levelTab, int page, int pageSize) {
        return pageMembers(account, nickname, null, levelTab, null, null, page, pageSize);
    }

    private MemberPageVO pageMembers(String account, String nickname, String tab, String levelTab,
                                     LocalDate startDate, LocalDate endDate, int page, int pageSize) {
        var spec = MemberSpecifications.listFilter(account, nickname, tab, levelTab, startDate, endDate);
        Page<MemberEntity> result = memberRepository.findAll(spec,
                PageRequest.of(Math.max(page - 1, 0), pageSize, Sort.by(Sort.Direction.DESC, "registerTime")));
        Map<String, String> levelNames = MemberSupport.levelNameMap(levelRepository.findByDeletedFalseOrderBySortNumAsc());
        MemberPageVO vo = new MemberPageVO();
        vo.setList(result.getContent().stream().map(m -> MemberSupport.toListVO(m, levelNames)).toList());
        vo.setTotal(result.getTotalElements());
        vo.setPage(page);
        vo.setPageSize(pageSize);
        vo.setTotalPages((int) Math.ceil((double) result.getTotalElements() / pageSize));
        return vo;
    }

    public MemberDetailVO detail(String userNo) {
        MemberEntity m = memberRepository.findByUserNoAndDeletedFalse(userNo)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        Map<String, String> levelNames = MemberSupport.levelNameMap(levelRepository.findByDeletedFalseOrderBySortNumAsc());
        MemberDetailVO vo = new MemberDetailVO();
        vo.setUserId(m.getUserNo());
        vo.setNickname(m.getNickname());
        vo.setAvatar(m.getAvatar());
        vo.setPhoneMasked(MemberSupport.maskPhone(m.getPhone()));
        vo.setLevelKey(m.getLevelCode());
        vo.setLevel(levelNames.getOrDefault(m.getLevelCode(), m.getLevelCode()));
        vo.setStatusText(MemberSupport.statusText(m.getStatus()));
        vo.setIp(m.getLastIp() == null ? "-" : m.getLastIp());
        vo.setSource(m.getSource() == null ? "APP" : m.getSource());
        vo.setTags(buildUserTags(m.getUserNo()));
        vo.setBirthday(m.getBirthday() == null ? "-" : m.getBirthday());
        vo.setRegisterTime(MemberSupport.formatTime(m.getRegisterTime()));
        vo.setCity(m.getCity() == null ? "-" : m.getCity());
        vo.setGender(MemberSupport.genderText(m.getGender()));
        vo.setConsumeAmount(m.getConsumeAmount());
        vo.setOrderCount(m.getOrderCount());
        vo.setPoints(m.getPoints());
        vo.setGrowth(m.getGrowthValue());
        vo.setCouponCount(m.getCouponCount());
        vo.setReviewCount(m.getReviewCount());
        vo.setReturnCount(m.getReturnCount());
        vo.setLoginCount(m.getLoginCount());
        vo.setFavoriteProducts(m.getFavoriteProducts());
        vo.setFavoriteTopics(m.getFavoriteTopics());
        vo.setOrderFriends(m.getOrderFriends());
        vo.setLotteryCount(m.getLotteryCount());
        vo.setRemark(m.getRemark());
        vo.setAccount(m.getPhone());
        vo.setPermissions(MemberSupport.parsePermissions(m.getPermissionsJson()));
        vo.setAddresses(addressRepository.findByUserNoOrderByIsDefaultDesc(m.getUserNo()).stream()
                .map(a -> {
                    MemberAddressVO av = new MemberAddressVO();
                    av.setName(a.getContactName());
                    av.setPhone(a.getPhone());
                    av.setRegion(a.getRegion());
                    av.setDetail(a.getDetailAddress());
                    return av;
                }).toList());
        vo.setOrders(orderRepository.findTop20ByReceiverPhoneAndDeletedFalseOrderByCreateTimeDesc(m.getPhone()).stream()
                .map(o -> {
                    MemberOrderBriefVO ov = new MemberOrderBriefVO();
                    ov.setId(o.getOrderNo());
                    ov.setTime(MemberSupport.formatTime(o.getCreateTime()));
                    ov.setAmount(o.getActualAmount());
                    ov.setPayMethod(o.getPayMethod() == null ? "-" : o.getPayMethod());
                    ov.setSource(o.getOrderSource() == null ? "-" : o.getOrderSource());
                    ov.setStatus(ORDER_STATUS_TEXT.getOrDefault(o.getOrderStatus(), o.getOrderStatus()));
                    return ov;
                }).toList());
        return vo;
    }

    private String buildUserTags(String userNo) {
        List<MemberTagRelEntity> rels = tagRelRepository.findByUserNo(userNo);
        if (rels.isEmpty()) {
            return "-";
        }
        Map<String, String> tagNames = tagRepository.findByDeletedFalseOrderByIdDesc().stream()
                .collect(Collectors.toMap(MemberTagEntity::getTagCode, MemberTagEntity::getTagName, (a, b) -> a));
        return rels.stream()
                .map(r -> tagNames.getOrDefault(r.getTagCode(), r.getTagCode()))
                .collect(Collectors.joining("、"));
    }

    @Transactional
    public void update(MemberUpdateRequest req) {
        MemberEntity m = memberRepository.findByUserNoAndDeletedFalse(req.getUserId())
                .orElseThrow(() -> new BusinessException("用户不存在"));
        if (req.getPhone() != null && !req.getPhone().isBlank()) {
            m.setPhone(req.getPhone().trim());
        }
        if (req.getLevel() != null) {
            m.setLevelCode(req.getLevel());
        }
        if (req.getAvatar() != null) {
            m.setAvatar(req.getAvatar());
        }
        if (req.getGender() != null) {
            m.setGender(req.getGender());
        }
        if (req.getCity() != null && !req.getCity().isEmpty()) {
            m.setCityCodes(String.join(",", req.getCity()));
        }
        if (req.getPermissions() != null && !req.getPermissions().isEmpty()) {
            m.setPermissionsJson(Jsons.toJson(req.getPermissions()));
            m.setStatus(MemberSupport.resolvePrimaryStatus(req.getPermissions()));
        }
        if (req.getRemark() != null) {
            m.setRemark(req.getRemark());
        }
        memberRepository.save(m);
    }

    public byte[] exportMembers(String account, String nickname, String tab) {
        var spec = MemberSpecifications.listFilter(account, nickname, tab, null, null, null);
        List<MemberEntity> list = memberRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "registerTime"));
        Map<String, String> levelNames = MemberSupport.levelNameMap(levelRepository.findByDeletedFalseOrderBySortNumAsc());
        StringBuilder sb = new StringBuilder("\uFEFF");
        sb.append("用户编号,昵称,账号,等级,消费金额,订单数,积分,成长值,状态,注册时间\n");
        for (MemberEntity m : list) {
            MemberListVO vo = MemberSupport.toListVO(m, levelNames);
            sb.append(csv(vo.getId())).append(',')
                    .append(csv(vo.getNickname())).append(',')
                    .append(csv(vo.getAccount())).append(',')
                    .append(csv(vo.getLevel())).append(',')
                    .append(vo.getConsumeAmount()).append(',')
                    .append(vo.getOrderCount()).append(',')
                    .append(vo.getPoints()).append(',')
                    .append(vo.getGrowth()).append(',')
                    .append(csv(vo.getStatusText())).append(',')
                    .append(csv(vo.getRegisterTime())).append('\n');
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public List<MemberLevelVO> levelList(String name) {
        return levelRepository.findByDeletedFalseOrderBySortNumAsc().stream()
                .filter(lv -> name == null || name.isBlank()
                        || lv.getLevelName().contains(name.trim())
                        || lv.getLevelCode().contains(name.trim()))
                .map(this::toLevelVO)
                .toList();
    }

    public MemberLevelVO levelDetail(String levelCode) {
        MemberLevelEntity lv = levelRepository.findByLevelCodeAndDeletedFalse(levelCode)
                .orElseThrow(() -> new BusinessException("等级不存在"));
        return toLevelVO(lv);
    }

    @Transactional
    public String saveLevel(MemberLevelSaveRequest req) {
        MemberLevelEntity lv;
        if (req.getId() != null && !req.getId().isBlank()) {
            lv = levelRepository.findByLevelCodeAndDeletedFalse(req.getId())
                    .orElseThrow(() -> new BusinessException("等级不存在"));
        } else {
            lv = new MemberLevelEntity();
            lv.setLevelCode("LV" + System.currentTimeMillis() % 100000);
            lv.setSortNum((int) levelRepository.count() + 1);
        }
        lv.setLevelName(req.getName());
        lv.setGrowthPoint(req.getGrowthPoint() == null ? 0 : req.getGrowthPoint());
        lv.setIsDefault(Boolean.TRUE.equals(req.getIsDefault()));
        lv.setFreeShipAmount(req.getFreeShipAmount() == null ? BigDecimal.valueOf(40) : BigDecimal.valueOf(req.getFreeShipAmount()));
        lv.setFreeShipTimes(req.getFreeShipTimes() == null ? 2 : req.getFreeShipTimes());
        lv.setReviewGrowth(req.getReviewGrowth() == null ? 5 : req.getReviewGrowth());
        lv.setReviewTimes(req.getReviewTimes() == null ? 10 : req.getReviewTimes());
        if (req.getPrivileges() != null) {
            lv.setPrivilegesJson(Jsons.toJson(req.getPrivileges()));
        }
        if (Boolean.TRUE.equals(req.getIsDefault())) {
            levelRepository.findByDeletedFalseOrderBySortNumAsc().forEach(other -> {
                if (!other.getLevelCode().equals(lv.getLevelCode())) {
                    other.setIsDefault(false);
                    levelRepository.save(other);
                }
            });
        }
        levelRepository.save(lv);
        refreshMemberLevelsByGrowth();
        return lv.getLevelCode();
    }

    @Transactional
    public void deleteLevel(String levelCode) {
        MemberLevelEntity lv = levelRepository.findByLevelCodeAndDeletedFalse(levelCode)
                .orElseThrow(() -> new BusinessException("等级不存在"));
        if (Boolean.TRUE.equals(lv.getIsDefault())) {
            throw new BusinessException("默认等级不可删除");
        }
        lv.setDeleted(true);
        levelRepository.save(lv);
    }

    public List<LevelOptionVO> levelOptions() {
        return levelRepository.findByDeletedFalseOrderBySortNumAsc().stream()
                .map(lv -> new LevelOptionVO(lv.getLevelName(), lv.getLevelCode()))
                .toList();
    }

    private void refreshMemberLevelsByGrowth() {
        List<MemberLevelEntity> levels = levelRepository.findByDeletedFalseOrderBySortNumAsc();
        List<MemberEntity> members = memberRepository.findByDeletedFalse();
        for (MemberEntity m : members) {
            String code = resolveLevelByGrowth(m.getGrowthValue(), levels);
            if (code != null) {
                m.setLevelCode(code);
                memberRepository.save(m);
            }
        }
    }

    private String resolveLevelByGrowth(int growth, List<MemberLevelEntity> levels) {
        MemberLevelEntity matched = null;
        for (MemberLevelEntity lv : levels) {
            if (growth >= lv.getGrowthPoint()) {
                matched = lv;
            }
        }
        return matched == null ? null : matched.getLevelCode();
    }

    public List<MemberTagVO> tagList(String keyword) {
        return tagRepository.findByDeletedFalseOrderByIdDesc().stream()
                .filter(t -> keyword == null || keyword.isBlank()
                        || t.getTagName().contains(keyword.trim())
                        || t.getTagCode().contains(keyword.trim()))
                .map(t -> {
                    MemberTagVO vo = new MemberTagVO();
                    vo.setId(t.getTagCode());
                    vo.setName(t.getTagName());
                    vo.setMemberCount(t.getMemberCount());
                    MemberTagSaveRequest rule = TagRuleMatcher.parseRule(t.getRuleJson());
                    vo.setCondition(TagRuleMatcher.buildConditionText(rule));
                    return vo;
                }).toList();
    }

    public MemberTagSaveRequest tagDetail(String tagCode) {
        MemberTagEntity tag = tagRepository.findByTagCodeAndDeletedFalse(tagCode)
                .orElseThrow(() -> new BusinessException("标签不存在"));
        MemberTagSaveRequest rule = TagRuleMatcher.parseRule(tag.getRuleJson());
        rule.setId(tag.getTagCode());
        rule.setName(tag.getTagName());
        return rule;
    }

    @Transactional
    public String saveTag(MemberTagSaveRequest req) {
        MemberTagEntity tag;
        if (req.getId() != null && !req.getId().isBlank()) {
            tag = tagRepository.findByTagCodeAndDeletedFalse(req.getId()).orElseGet(() -> {
                MemberTagEntity e = new MemberTagEntity();
                e.setTagCode(req.getId());
                return e;
            });
        } else {
            tag = new MemberTagEntity();
            tag.setTagCode("TAG" + (tagRepository.count() + 1));
        }
        tag.setTagName(req.getName());
        tag.setRuleJson(TagRuleMatcher.toRuleJson(req));
        tagRepository.save(tag);
        applyTagRules(tag.getTagCode(), req);
        return tag.getTagCode();
    }

    private void applyTagRules(String tagCode, MemberTagSaveRequest rule) {
        tagRelRepository.deleteByTagCode(tagCode);
        List<MemberEntity> members = memberRepository.findByDeletedFalse();
        int count = 0;
        for (MemberEntity m : members) {
            if (TagRuleMatcher.matches(rule, m)) {
                MemberTagRelEntity rel = new MemberTagRelEntity();
                rel.setTagCode(tagCode);
                rel.setUserNo(m.getUserNo());
                tagRelRepository.save(rel);
                count++;
            }
        }
        MemberTagEntity tag = tagRepository.findByTagCodeAndDeletedFalse(tagCode).orElseThrow();
        tag.setMemberCount(count);
        tagRepository.save(tag);
    }

    @Transactional
    public void deleteTag(String tagCode) {
        MemberTagEntity tag = tagRepository.findByTagCodeAndDeletedFalse(tagCode)
                .orElseThrow(() -> new BusinessException("标签不存在"));
        tag.setDeleted(true);
        tagRepository.save(tag);
        tagRelRepository.deleteByTagCode(tagCode);
    }

    public MemberPageVO tagUsers(String tagCode, int page, int pageSize) {
        List<String> userNos = tagRelRepository.findByTagCode(tagCode).stream()
                .map(MemberTagRelEntity::getUserNo).toList();
        if (userNos.isEmpty()) {
            MemberPageVO empty = new MemberPageVO();
            empty.setList(List.of());
            empty.setTotal(0);
            empty.setPage(page);
            empty.setPageSize(pageSize);
            empty.setTotalPages(0);
            return empty;
        }
        Specification<MemberEntity> spec = (root, query, cb) -> cb.and(
                cb.isFalse(root.get("deleted")),
                root.get("userNo").in(userNos)
        );
        Page<MemberEntity> result = memberRepository.findAll(spec,
                PageRequest.of(Math.max(page - 1, 0), pageSize, Sort.by(Sort.Direction.DESC, "registerTime")));
        Map<String, String> levelNames = MemberSupport.levelNameMap(levelRepository.findByDeletedFalseOrderBySortNumAsc());
        MemberPageVO vo = new MemberPageVO();
        vo.setList(result.getContent().stream().map(m -> MemberSupport.toListVO(m, levelNames)).toList());
        vo.setTotal(result.getTotalElements());
        vo.setPage(page);
        vo.setPageSize(pageSize);
        vo.setTotalPages((int) Math.ceil((double) result.getTotalElements() / pageSize));
        return vo;
    }

    @Transactional
    public void removeTagUser(String tagCode, String userNo) {
        tagRelRepository.deleteByTagCodeAndUserNo(tagCode, userNo);
        tagRepository.findByTagCodeAndDeletedFalse(tagCode).ifPresent(tag -> {
            tag.setMemberCount((int) tagRelRepository.countByTagCode(tagCode));
            tagRepository.save(tag);
        });
    }

    @Transactional
    public void adjust(GrowthAdjustRequest req) {
        MemberEntity m = memberRepository.findByUserNoAndDeletedFalse(req.getUserId())
                .orElseThrow(() -> new BusinessException("用户不存在"));
        if ("growth".equals(req.getType())) {
            if (req.getValue() == null) {
                throw new BusinessException("请填写调整值");
            }
            int before = m.getGrowthValue();
            int after = req.getValue();
            m.setGrowthValue(after);
            memberRepository.save(m);
            writeLedger(m.getUserNo(), "growth", "adjust", before, after - before, after, req.getRemark());
            refreshMemberLevelsByGrowth();
        } else if ("points".equals(req.getType())) {
            if (req.getValue() == null) {
                throw new BusinessException("请填写调整值");
            }
            int before = m.getPoints();
            int after = req.getValue();
            m.setPoints(after);
            memberRepository.save(m);
            writeLedger(m.getUserNo(), "points", "adjust", before, after - before, after, req.getRemark());
        } else if ("settings".equals(req.getType())) {
            if (req.getLevel() != null) {
                m.setLevelCode(req.getLevel());
            }
            if (req.getPoints() != null) {
                m.setPoints(req.getPoints());
            }
            if (req.getGrowth() != null) {
                m.setGrowthValue(req.getGrowth());
            }
            memberRepository.save(m);
            refreshMemberLevelsByGrowth();
        }
    }

    @Transactional
    public void batchAdjust(GrowthBatchRequest req) {
        for (String userNo : req.getUserIds()) {
            MemberEntity m = memberRepository.findByUserNoAndDeletedFalse(userNo).orElse(null);
            if (m == null) {
                continue;
            }
            boolean isGrowth = "growth".equals(req.getAdjustType());
            int before = isGrowth ? m.getGrowthValue() : m.getPoints();
            int after = "set".equals(req.getMode()) ? req.getValue()
                    : before + (req.getValue() == null ? 0 : req.getValue());
            if (isGrowth) {
                m.setGrowthValue(Math.max(0, after));
            } else {
                m.setPoints(Math.max(0, after));
            }
            memberRepository.save(m);
            writeLedger(userNo, isGrowth ? "growth" : "points", "batch",
                    before, after - before, after, req.getRemark());
        }
        refreshMemberLevelsByGrowth();
    }

    public List<GrowthTaskVO> listTasks() {
        return taskRepository.findAllByOrderBySortNumAsc().stream().map(t -> {
            GrowthTaskVO vo = new GrowthTaskVO();
            vo.setTaskCode(t.getTaskCode());
            vo.setTaskName(t.getTaskName());
            vo.setGrowthReward(t.getGrowthReward());
            vo.setPointsReward(t.getPointsReward());
            vo.setEnabled(t.getEnabled());
            vo.setDescription(t.getDescription());
            vo.setSortNum(t.getSortNum());
            return vo;
        }).toList();
    }

    @Transactional
    public void saveTasks(List<GrowthTaskVO> tasks) {
        for (GrowthTaskVO vo : tasks) {
            GrowthTaskEntity t = taskRepository.findByTaskCode(vo.getTaskCode()).orElseGet(() -> {
                GrowthTaskEntity e = new GrowthTaskEntity();
                e.setTaskCode(vo.getTaskCode());
                return e;
            });
            t.setTaskName(vo.getTaskName());
            t.setGrowthReward(vo.getGrowthReward() == null ? 0 : vo.getGrowthReward());
            t.setPointsReward(vo.getPointsReward() == null ? 0 : vo.getPointsReward());
            t.setEnabled(vo.getEnabled() == null || vo.getEnabled());
            t.setDescription(vo.getDescription());
            t.setSortNum(vo.getSortNum() == null ? 0 : vo.getSortNum());
            taskRepository.save(t);
        }
    }

    public GrowthRulesVO getRules() {
        return loadConfig(CFG_RULES, GrowthRulesVO.class, defaultRules());
    }

    @Transactional
    public void saveRules(GrowthRulesVO rules) {
        saveConfig(CFG_RULES, rules);
    }

    public GrowthRewardsVO getRewards() {
        return loadConfig(CFG_REWARDS, GrowthRewardsVO.class, defaultRewards());
    }

    @Transactional
    public void saveRewards(GrowthRewardsVO rewards) {
        saveConfig(CFG_REWARDS, rewards);
    }

    public LedgerPageVO ledgerList(String ledgerType, String account, int page, int pageSize) {
        Specification<MemberLedgerEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (ledgerType != null && !ledgerType.isBlank() && !"all".equals(ledgerType)) {
                predicates.add(cb.equal(root.get("ledgerType"), ledgerType));
            }
            if (account != null && !account.isBlank()) {
                String kw = "%" + account.trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("userNo"), kw),
                        cb.like(root.get("remark"), kw)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<MemberLedgerEntity> result = ledgerRepository.findAll(spec,
                PageRequest.of(Math.max(page - 1, 0), pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        Map<String, MemberEntity> memberMap = memberRepository.findByDeletedFalse().stream()
                .collect(Collectors.toMap(MemberEntity::getUserNo, m -> m, (a, b) -> a));
        LedgerPageVO vo = new LedgerPageVO();
        vo.setList(result.getContent().stream().map(l -> toLedgerVO(l, memberMap)).toList());
        vo.setTotal(result.getTotalElements());
        vo.setPage(page);
        vo.setPageSize(pageSize);
        vo.setTotalPages((int) Math.ceil((double) result.getTotalElements() / pageSize));
        return vo;
    }

    public byte[] exportGrowth(String account, String levelTab) {
        MemberPageVO page = growthList(account, null, levelTab, 1, 10000);
        StringBuilder sb = new StringBuilder("\uFEFF");
        sb.append("用户编号,昵称,账号,等级,积分,成长值\n");
        for (MemberListVO vo : page.getList()) {
            sb.append(csv(vo.getId())).append(',')
                    .append(csv(vo.getNickname())).append(',')
                    .append(csv(vo.getAccount())).append(',')
                    .append(csv(vo.getLevel())).append(',')
                    .append(vo.getPoints()).append(',')
                    .append(vo.getGrowth()).append('\n');
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private void writeLedger(String userNo, String type, String changeType,
                             int before, int delta, int after, String remark) {
        MemberLedgerEntity ledger = new MemberLedgerEntity();
        ledger.setLedgerNo("LG" + System.currentTimeMillis() + userNo.hashCode() % 1000);
        ledger.setUserNo(userNo);
        ledger.setLedgerType(type);
        ledger.setChangeType(changeType);
        ledger.setBeforeQty(before);
        ledger.setChangeQty(delta);
        ledger.setAfterQty(after);
        ledger.setRemark(remark);
        ledger.setOperatorName("admin");
        ledger.setCreatedAt(LocalDateTime.now());
        ledgerRepository.save(ledger);
    }

    private MemberLevelVO toLevelVO(MemberLevelEntity lv) {
        MemberLevelVO vo = new MemberLevelVO();
        vo.setId(lv.getLevelCode());
        vo.setName(lv.getLevelName());
        vo.setIsDefault(lv.getIsDefault());
        vo.setGrowthPoint(lv.getGrowthPoint());
        vo.setFreeShipping(MemberSupport.freeShippingText(lv));
        vo.setReviewReward(MemberSupport.reviewRewardText(lv));
        vo.setSort(lv.getSortNum());
        vo.setFreeShipAmount(lv.getFreeShipAmount() == null ? 0 : lv.getFreeShipAmount().intValue());
        vo.setFreeShipTimes(lv.getFreeShipTimes());
        vo.setReviewGrowth(lv.getReviewGrowth());
        vo.setReviewTimes(lv.getReviewTimes());
        if (lv.getPrivilegesJson() != null) {
            try {
                vo.setPrivileges(MAPPER.readValue(lv.getPrivilegesJson(),
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, Boolean>>() {}));
            } catch (Exception ignored) {
                vo.setPrivileges(Map.of());
            }
        }
        return vo;
    }

    private MemberLedgerVO toLedgerVO(MemberLedgerEntity l, Map<String, MemberEntity> memberMap) {
        MemberLedgerVO vo = new MemberLedgerVO();
        vo.setLedgerNo(l.getLedgerNo());
        vo.setUserNo(l.getUserNo());
        MemberEntity m = memberMap.get(l.getUserNo());
        if (m != null) {
            vo.setNickname(m.getNickname());
            vo.setAccount(m.getPhone());
        }
        vo.setLedgerType(l.getLedgerType());
        vo.setChangeType(l.getChangeType());
        vo.setChangeTypeText(changeTypeText(l.getChangeType()));
        vo.setBeforeQty(l.getBeforeQty());
        vo.setChangeQty(l.getChangeQty());
        vo.setAfterQty(l.getAfterQty());
        vo.setRemark(l.getRemark());
        vo.setOperatorName(l.getOperatorName());
        vo.setCreatedAt(MemberSupport.formatTime(l.getCreatedAt()));
        return vo;
    }

    private String changeTypeText(String type) {
        return switch (type) {
            case "adjust" -> "手动调整";
            case "batch" -> "批量调整";
            case "order" -> "订单奖励";
            case "sign_in" -> "签到";
            case "review" -> "评价";
            case "task" -> "任务";
            default -> type;
        };
    }

    private GrowthRulesVO defaultRules() {
        GrowthRulesVO r = new GrowthRulesVO();
        r.setOrderGrowthPerYuan(1);
        r.setOrderPointsPerYuan(1);
        r.setSignInGrowth(5);
        r.setSignInPoints(5);
        r.setReviewGrowth(5);
        r.setReviewPoints(5);
        r.setGrowthExpireDays("365");
        r.setPointsExpireDays("365");
        return r;
    }

    private GrowthRewardsVO defaultRewards() {
        GrowthRewardsVO r = new GrowthRewardsVO();
        r.setRegisterEnabled(true);
        r.setRegisterGrowth(100);
        r.setRegisterPoints(100);
        r.setBirthdayEnabled(true);
        r.setBirthdayGrowth(50);
        r.setBirthdayPoints(50);
        r.setInviteEnabled(false);
        r.setInviteGrowth(20);
        r.setInvitePoints(20);
        return r;
    }

    private <T> T loadConfig(String key, Class<T> type, T defaultValue) {
        return configRepository.findByConfigKey(key)
                .map(c -> {
                    try {
                        return MAPPER.readValue(c.getConfigValue(), type);
                    } catch (Exception e) {
                        return defaultValue;
                    }
                })
                .orElse(defaultValue);
    }

    private void saveConfig(String key, Object value) {
        GrowthConfigEntity cfg = configRepository.findByConfigKey(key).orElseGet(() -> {
            GrowthConfigEntity e = new GrowthConfigEntity();
            e.setConfigKey(key);
            return e;
        });
        try {
            cfg.setConfigValue(MAPPER.writeValueAsString(value));
        } catch (Exception e) {
            throw new BusinessException("配置保存失败");
        }
        configRepository.save(cfg);
    }

    private static String csv(String v) {
        if (v == null) {
            return "";
        }
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
            return "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }
}
