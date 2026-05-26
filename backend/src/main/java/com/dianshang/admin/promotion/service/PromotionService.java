package com.dianshang.admin.promotion.service;

import com.dianshang.admin.common.BusinessException;
import com.dianshang.admin.common.Jsons;
import com.dianshang.admin.product.entity.Product;
import com.dianshang.admin.product.repository.ProductRepository;
import com.dianshang.admin.promotion.dto.*;
import com.dianshang.admin.promotion.entity.*;
import com.dianshang.admin.promotion.repository.*;
import com.dianshang.admin.promotion.support.PromotionSupport;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class PromotionService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final Map<String, String> STATUS_LABEL = Map.of(
            "pending", "待开始",
            "active", "进行中",
            "offline", "已下架",
            "ended", "已结束"
    );

    private final SeckillActivityRepository seckillActivityRepository;
    private final SeckillTimeSlotRepository seckillTimeSlotRepository;
    private final SeckillSkuRepository seckillSkuRepository;
    private final GroupBuyActivityRepository groupBuyActivityRepository;
    private final GroupBuyTimeSlotRepository groupBuyTimeSlotRepository;
    private final GroupBuySkuRepository groupBuySkuRepository;
    private final CouponRepository couponRepository;
    private final CouponClaimRepository couponClaimRepository;
    private final ProductRepository productRepository;

    public PromotionService(SeckillActivityRepository seckillActivityRepository,
                            SeckillTimeSlotRepository seckillTimeSlotRepository,
                            SeckillSkuRepository seckillSkuRepository,
                            GroupBuyActivityRepository groupBuyActivityRepository,
                            GroupBuyTimeSlotRepository groupBuyTimeSlotRepository,
                            GroupBuySkuRepository groupBuySkuRepository,
                            CouponRepository couponRepository,
                            CouponClaimRepository couponClaimRepository,
                            ProductRepository productRepository) {
        this.seckillActivityRepository = seckillActivityRepository;
        this.seckillTimeSlotRepository = seckillTimeSlotRepository;
        this.seckillSkuRepository = seckillSkuRepository;
        this.groupBuyActivityRepository = groupBuyActivityRepository;
        this.groupBuyTimeSlotRepository = groupBuyTimeSlotRepository;
        this.groupBuySkuRepository = groupBuySkuRepository;
        this.couponRepository = couponRepository;
        this.couponClaimRepository = couponClaimRepository;
        this.productRepository = productRepository;
    }

    // ---------- Seckill Activity ----------

    public PromoActivityPageVO seckillList(String keyword, String tab, LocalDate startDate, LocalDate endDate,
                                           int page, int pageSize) {
        return pageSeckillActivities(keyword, tab, startDate, endDate, page, pageSize);
    }

    private PromoActivityPageVO pageSeckillActivities(String keyword, String tab, LocalDate startDate,
                                                      LocalDate endDate, int page, int pageSize) {
        Specification<SeckillActivityEntity> spec = activitySpec(keyword, startDate, endDate);
        Page<SeckillActivityEntity> result = seckillActivityRepository.findAll(spec,
                PageRequest.of(Math.max(page - 1, 0), pageSize, Sort.by(Sort.Direction.DESC, "startTime")));
        List<PromoActivityVO> list = result.getContent().stream()
                .map(this::toSeckillActivityVO)
                .filter(vo -> tab == null || tab.isBlank() || "all".equals(tab) || tab.equals(vo.getStatus()))
                .toList();
        PromoActivityPageVO vo = new PromoActivityPageVO();
        vo.setList(list);
        vo.setTotal(result.getTotalElements());
        vo.setPage(page);
        vo.setPageSize(pageSize);
        vo.setTotalPages((int) Math.ceil((double) result.getTotalElements() / pageSize));
        return vo;
    }

    @Transactional
    public String saveSeckill(PromoActivitySaveRequest req) {
        SeckillActivityEntity entity;
        if (req.getId() != null && !req.getId().isBlank()) {
            entity = seckillActivityRepository.findByActivityCodeAndDeletedFalse(req.getId())
                    .orElseThrow(() -> new BusinessException("活动不存在"));
        } else {
            entity = new SeckillActivityEntity();
            entity.setActivityCode("SK" + System.currentTimeMillis() % 10000000);
        }
        entity.setTitle(req.getTitle());
        entity.setStartTime(PromotionSupport.parseDateTime(req.getStartTime()));
        entity.setEndTime(PromotionSupport.parseDateTime(req.getEndTime()));
        entity.setOnline(req.getOnline() == null || req.getOnline());
        seckillActivityRepository.save(entity);
        updateSeckillWarning(entity.getActivityCode());
        return entity.getActivityCode();
    }

    @Transactional
    public void deleteSeckill(String activityCode) {
        SeckillActivityEntity entity = seckillActivityRepository.findByActivityCodeAndDeletedFalse(activityCode)
                .orElseThrow(() -> new BusinessException("活动不存在"));
        entity.setDeleted(true);
        seckillActivityRepository.save(entity);
    }

    @Transactional
    public void toggleSeckillOnline(String activityCode, boolean online) {
        SeckillActivityEntity entity = seckillActivityRepository.findByActivityCodeAndDeletedFalse(activityCode)
                .orElseThrow(() -> new BusinessException("活动不存在"));
        entity.setOnline(online);
        seckillActivityRepository.save(entity);
    }

    // ---------- Seckill Time ----------

    public List<TimeSlotVO> seckillTimeList() {
        return seckillTimeSlotRepository.findAllByOrderByStartTimeAsc().stream().map(this::toTimeSlotVO).toList();
    }

    @Transactional
    public String saveSeckillTime(TimeSlotSaveRequest req) {
        SeckillTimeSlotEntity slot;
        if (req.getId() != null && !req.getId().isBlank()) {
            slot = seckillTimeSlotRepository.findBySlotCode(req.getId())
                    .orElseThrow(() -> new BusinessException("时段不存在"));
        } else {
            slot = new SeckillTimeSlotEntity();
            slot.setSlotCode("ST" + (seckillTimeSlotRepository.count() + 1));
        }
        slot.setSlotName(req.getName());
        slot.setStartTime(req.getStart());
        slot.setEndTime(req.getEnd());
        slot.setEnabled(req.getEnabled() == null || req.getEnabled());
        seckillTimeSlotRepository.save(slot);
        return slot.getSlotCode();
    }

    @Transactional
    public void deleteSeckillTime(String slotCode) {
        seckillTimeSlotRepository.findBySlotCode(slotCode).ifPresent(seckillTimeSlotRepository::delete);
    }

    @Transactional
    public void toggleSeckillTime(String slotCode, boolean enabled) {
        SeckillTimeSlotEntity slot = seckillTimeSlotRepository.findBySlotCode(slotCode)
                .orElseThrow(() -> new BusinessException("时段不存在"));
        slot.setEnabled(enabled);
        seckillTimeSlotRepository.save(slot);
    }

    public List<TimeSlotVO> seckillActivitySlots(String activityCode) {
        return seckillTimeList();
    }

    // ---------- Seckill SKU ----------

    public List<SeckillSkuVO> seckillSkuList(String activityCode, String slotCode) {
        return seckillSkuRepository.findByActivityCodeAndSlotCodeOrderBySortNumAsc(activityCode, slotCode).stream()
                .map(this::toSeckillSkuVO).toList();
    }

    @Transactional
    public void saveSeckillSku(SeckillSkuSaveRequest req) {
        seckillSkuRepository.deleteByActivityCodeAndSlotCode(req.getActivityId(), req.getTimeId());
        if (req.getItems() == null) {
            updateSeckillWarning(req.getActivityId());
            return;
        }
        int sort = 1;
        for (SeckillSkuVO item : req.getItems()) {
            SeckillSkuEntity sku = new SeckillSkuEntity();
            sku.setActivityCode(req.getActivityId());
            sku.setSlotCode(req.getTimeId());
            sku.setProductNo(item.getProductCode());
            sku.setProductName(item.getName());
            sku.setPrice(item.getPrice() == null ? BigDecimal.ZERO : item.getPrice());
            sku.setSeckillPrice(item.getSeckillPrice() == null ? sku.getPrice() : item.getSeckillPrice());
            sku.setSeckillQty(item.getSeckillQty() == null ? 0 : item.getSeckillQty());
            sku.setRemainStock(item.getRemainStock() == null ? sku.getSeckillQty() : item.getRemainStock());
            sku.setWarningStock(item.getWarningStock() == null ? 0 : item.getWarningStock());
            sku.setLimitQty(item.getLimitQty() == null ? 1 : item.getLimitQty());
            sku.setSortNum(item.getSort() == null ? sort++ : item.getSort());
            seckillSkuRepository.save(sku);
        }
        updateSeckillWarning(req.getActivityId());
    }

    // ---------- Group Buy ----------

    public PromoActivityPageVO groupBuyList(String keyword, String tab, LocalDate startDate, LocalDate endDate,
                                            int page, int pageSize) {
        Specification<GroupBuyActivityEntity> spec = groupActivitySpec(keyword, startDate, endDate);
        Page<GroupBuyActivityEntity> result = groupBuyActivityRepository.findAll(spec,
                PageRequest.of(Math.max(page - 1, 0), pageSize, Sort.by(Sort.Direction.DESC, "startTime")));
        List<PromoActivityVO> list = result.getContent().stream()
                .map(this::toGroupActivityVO)
                .filter(vo -> tab == null || tab.isBlank() || "all".equals(tab) || tab.equals(vo.getStatus()))
                .toList();
        PromoActivityPageVO vo = new PromoActivityPageVO();
        vo.setList(list);
        vo.setTotal(result.getTotalElements());
        vo.setPage(page);
        vo.setPageSize(pageSize);
        vo.setTotalPages((int) Math.ceil((double) result.getTotalElements() / pageSize));
        return vo;
    }

    @Transactional
    public String saveGroupBuy(PromoActivitySaveRequest req) {
        GroupBuyActivityEntity entity;
        if (req.getId() != null && !req.getId().isBlank()) {
            entity = groupBuyActivityRepository.findByActivityCodeAndDeletedFalse(req.getId())
                    .orElseThrow(() -> new BusinessException("活动不存在"));
        } else {
            entity = new GroupBuyActivityEntity();
            entity.setActivityCode("GB" + System.currentTimeMillis() % 10000000);
        }
        entity.setTitle(req.getTitle());
        entity.setStartTime(PromotionSupport.parseDateTime(req.getStartTime()));
        entity.setEndTime(PromotionSupport.parseDateTime(req.getEndTime()));
        entity.setOnline(req.getOnline() == null || req.getOnline());
        groupBuyActivityRepository.save(entity);
        updateGroupWarning(entity.getActivityCode());
        return entity.getActivityCode();
    }

    @Transactional
    public void deleteGroupBuy(String activityCode) {
        GroupBuyActivityEntity entity = groupBuyActivityRepository.findByActivityCodeAndDeletedFalse(activityCode)
                .orElseThrow(() -> new BusinessException("活动不存在"));
        entity.setDeleted(true);
        groupBuyActivityRepository.save(entity);
    }

    @Transactional
    public void toggleGroupBuyOnline(String activityCode, boolean online) {
        GroupBuyActivityEntity entity = groupBuyActivityRepository.findByActivityCodeAndDeletedFalse(activityCode)
                .orElseThrow(() -> new BusinessException("活动不存在"));
        entity.setOnline(online);
        groupBuyActivityRepository.save(entity);
    }

    public List<TimeSlotVO> groupBuyTimeList(String activityCode) {
        return groupBuyTimeSlotRepository.findByActivityCodeOrderByStartTimeAsc(activityCode).stream()
                .map(this::toGroupTimeSlotVO).toList();
    }

    @Transactional
    public String saveGroupBuyTime(TimeSlotSaveRequest req) {
        if (req.getActivityId() == null || req.getActivityId().isBlank()) {
            throw new BusinessException("活动编号不能为空");
        }
        groupBuyActivityRepository.findByActivityCodeAndDeletedFalse(req.getActivityId())
                .orElseThrow(() -> new BusinessException("活动不存在"));
        GroupBuyTimeSlotEntity slot;
        if (req.getId() != null && !req.getId().isBlank()) {
            slot = groupBuyTimeSlotRepository.findByActivityCodeAndSlotCode(req.getActivityId(), req.getId())
                    .orElseThrow(() -> new BusinessException("时段不存在"));
        } else {
            slot = new GroupBuyTimeSlotEntity();
            slot.setActivityCode(req.getActivityId());
            slot.setSlotCode("GT" + System.currentTimeMillis() % 100000);
        }
        slot.setSlotName(req.getName());
        slot.setStartTime(req.getStart());
        slot.setEndTime(req.getEnd());
        slot.setEnabled(req.getEnabled() == null || req.getEnabled());
        groupBuyTimeSlotRepository.save(slot);
        return slot.getSlotCode();
    }

    @Transactional
    public void deleteGroupBuyTime(String activityCode, String slotCode) {
        groupBuyTimeSlotRepository.deleteByActivityCodeAndSlotCode(activityCode, slotCode);
    }

    @Transactional
    public void toggleGroupBuyTime(String activityCode, String slotCode, boolean enabled) {
        GroupBuyTimeSlotEntity slot = groupBuyTimeSlotRepository.findByActivityCodeAndSlotCode(activityCode, slotCode)
                .orElseThrow(() -> new BusinessException("时段不存在"));
        slot.setEnabled(enabled);
        groupBuyTimeSlotRepository.save(slot);
    }

    public List<GroupBuySkuVO> groupBuySkuList(String activityCode, String slotCode) {
        return groupBuySkuRepository.findByActivityCodeAndSlotCodeOrderBySortNumAsc(activityCode, slotCode).stream()
                .map(this::toGroupSkuVO).toList();
    }

    @Transactional
    public void saveGroupBuySku(GroupBuySkuSaveRequest req) {
        groupBuySkuRepository.deleteByActivityCodeAndSlotCode(req.getActivityId(), req.getTimeId());
        if (req.getItems() == null) {
            updateGroupWarning(req.getActivityId());
            return;
        }
        int sort = 1;
        for (GroupBuySkuVO item : req.getItems()) {
            GroupBuySkuEntity sku = new GroupBuySkuEntity();
            sku.setActivityCode(req.getActivityId());
            sku.setSlotCode(req.getTimeId());
            sku.setProductNo(item.getProductCode());
            sku.setProductName(item.getName());
            sku.setPrice(item.getPrice() == null ? BigDecimal.ZERO : item.getPrice());
            sku.setGroupPrice(item.getGroupPrice() == null ? sku.getPrice() : item.getGroupPrice());
            sku.setGroupSize(item.getGroupSize() == null ? 2 : item.getGroupSize());
            sku.setGroupQty(item.getGroupQty() == null ? 0 : item.getGroupQty());
            sku.setRemainStock(item.getRemainStock() == null ? 0 : item.getRemainStock());
            sku.setWarningStock(item.getWarningStock() == null ? 0 : item.getWarningStock());
            sku.setLimitQty(item.getLimitQty() == null ? 1 : item.getLimitQty());
            sku.setSortNum(item.getSort() == null ? sort++ : item.getSort());
            if (item.getAttrs() != null) {
                sku.setAttrsJson(Jsons.toJson(item.getAttrs()));
            }
            groupBuySkuRepository.save(sku);
        }
        updateGroupWarning(req.getActivityId());
    }

    // ---------- Coupon ----------

    public CouponPageVO couponList(String keyword, String type, String tab, int page, int pageSize) {
        Specification<CouponEntity> spec = couponSpec(keyword, type);
        Page<CouponEntity> result = couponRepository.findAll(spec,
                PageRequest.of(Math.max(page - 1, 0), pageSize, Sort.by(Sort.Direction.DESC, "startTime")));
        List<CouponVO> list = result.getContent().stream()
                .map(this::toCouponVO)
                .filter(vo -> tab == null || tab.isBlank() || "all".equals(tab) || tab.equals(vo.getStatus()))
                .toList();
        CouponPageVO vo = new CouponPageVO();
        vo.setList(list);
        vo.setTotal(result.getTotalElements());
        vo.setPage(page);
        vo.setPageSize(pageSize);
        vo.setTotalPages((int) Math.ceil((double) result.getTotalElements() / pageSize));
        return vo;
    }

    public CouponDetailVO couponDetail(String couponCode) {
        CouponEntity c = couponRepository.findByCouponCodeAndDeletedFalse(couponCode)
                .orElseThrow(() -> new BusinessException("优惠券不存在"));
        CouponDetailVO vo = new CouponDetailVO();
        vo.setId(c.getCouponCode());
        vo.setName(c.getCouponName());
        vo.setType(PromotionSupport.couponTypeLabel(c.getCouponType()));
        vo.setProducts(scopeProductsText(c));
        vo.setThreshold(PromotionSupport.thresholdText(c.getThresholdAmount(), c.getFaceValue()));
        vo.setFaceValue(c.getFaceValue());
        vo.setThresholdAmount(c.getThresholdAmount());
        vo.setCouponType(c.getCouponType());
        vo.setStatus(STATUS_LABEL.getOrDefault(resolveCouponStatus(c), resolveCouponStatus(c)));
        vo.setTimeRange(PromotionSupport.formatTime(c.getStartTime()) + " 至 " + PromotionSupport.formatTime(c.getEndTime()));
        vo.setValidity(c.getValidityDays() + "天");
        int total = c.getIssueQty() < 0 ? c.getClaimedQty() + 5000 : c.getIssueQty();
        vo.setTotalIssue(total);
        vo.setRemain(Math.max(0, total - c.getClaimedQty()));
        vo.setClaimed(c.getClaimedQty());
        vo.setUsed(c.getUsedQty());
        vo.setPending((int) couponClaimRepository.countByCouponCodeAndStatus(couponCode, "pending"));
        vo.setExpired((int) couponClaimRepository.countByCouponCodeAndStatus(couponCode, "expired"));
        vo.setScopeType(c.getScopeType());
        vo.setOnline(c.getOnline());
        vo.setStartTime(PromotionSupport.formatTime(c.getStartTime()));
        vo.setEndTime(PromotionSupport.formatTime(c.getEndTime()));
        vo.setIssueQty(c.getIssueQty());
        vo.setPlatform(c.getPlatform());
        parseScopeIds(c, vo);
        return vo;
    }

    @Transactional
    public String saveCoupon(CouponSaveRequest req) {
        CouponEntity c;
        if (req.getId() != null && !req.getId().isBlank()) {
            c = couponRepository.findByCouponCodeAndDeletedFalse(req.getId())
                    .orElseThrow(() -> new BusinessException("优惠券不存在"));
        } else {
            c = new CouponEntity();
            c.setCouponCode("CP" + System.currentTimeMillis() % 10000000);
        }
        c.setCouponName(req.getName());
        c.setCouponType(req.getType());
        c.setScopeType(req.getScopeType());
        c.setScopeJson(buildScopeJson(req));
        c.setThresholdAmount(req.getThresholdAmount() == null ? BigDecimal.ZERO : req.getThresholdAmount());
        c.setFaceValue(req.getFaceValue() == null ? BigDecimal.ZERO : req.getFaceValue());
        c.setIssueQty(req.getIssueQty() == null ? -1 : req.getIssueQty());
        c.setPlatform(req.getPlatform());
        c.setValidityDays(req.getValidityDays() == null ? 15 : req.getValidityDays());
        if (req.getStartTime() != null) {
            c.setStartTime(PromotionSupport.parseDateTime(req.getStartTime()));
        }
        if (req.getEndTime() != null) {
            c.setEndTime(PromotionSupport.parseDateTime(req.getEndTime()));
        }
        c.setOnline(req.getOnline() == null || req.getOnline());
        couponRepository.save(c);
        return c.getCouponCode();
    }

    @Transactional
    public void deleteCoupon(String couponCode) {
        CouponEntity c = couponRepository.findByCouponCodeAndDeletedFalse(couponCode)
                .orElseThrow(() -> new BusinessException("优惠券不存在"));
        c.setDeleted(true);
        couponRepository.save(c);
    }

    @Transactional
    public void toggleCouponOnline(String couponCode, boolean online) {
        CouponEntity c = couponRepository.findByCouponCodeAndDeletedFalse(couponCode)
                .orElseThrow(() -> new BusinessException("优惠券不存在"));
        c.setOnline(online);
        couponRepository.save(c);
    }

    public List<CouponHistoryVO> couponHistory(String couponCode, String tab, String member, String orderId) {
        return couponClaimRepository.findByCouponCodeOrderByClaimTimeDesc(couponCode).stream()
                .filter(h -> tab == null || tab.isBlank() || "all".equals(tab) || tab.equals(h.getStatus()))
                .filter(h -> member == null || member.isBlank()
                        || (h.getMemberPhone() != null && h.getMemberPhone().contains(member))
                        || (h.getMemberNo() != null && h.getMemberNo().contains(member)))
                .filter(h -> orderId == null || orderId.isBlank()
                        || (h.getOrderNo() != null && h.getOrderNo().contains(orderId)))
                .map(this::toHistoryVO)
                .toList();
    }

    // ---------- Product Picker ----------

    public List<ProductPickerVO> productPicker(String keyword) {
        List<Product> products = productRepository.findAll().stream()
                .filter(p -> !Boolean.TRUE.equals(p.getDeleted()))
                .filter(p -> "on".equals(p.getStatus()))
                .toList();
        return products.stream()
                .filter(p -> keyword == null || keyword.isBlank()
                        || p.getTitle().contains(keyword)
                        || p.getProductNo().contains(keyword))
                .limit(50)
                .map(p -> {
                    ProductPickerVO vo = new ProductPickerVO();
                    vo.setId(p.getProductNo());
                    vo.setName(p.getTitle());
                    vo.setPrice(p.getDiscountPrice());
                    vo.setStock(p.getStock());
                    vo.setThumb(p.getThumb());
                    return vo;
                })
                .toList();
    }

    // ---------- helpers ----------

    private void updateSeckillWarning(String activityCode) {
        seckillActivityRepository.findByActivityCodeAndDeletedFalse(activityCode).ifPresent(act -> {
            long low = seckillSkuRepository.findByActivityCode(activityCode).stream()
                    .filter(s -> s.getRemainStock() <= s.getWarningStock()).count();
            PromotionSupport.refreshSeckillWarning(act, low);
            seckillActivityRepository.save(act);
        });
    }

    private void updateGroupWarning(String activityCode) {
        groupBuyActivityRepository.findByActivityCodeAndDeletedFalse(activityCode).ifPresent(act -> {
            long low = groupBuySkuRepository.findByActivityCode(activityCode).stream()
                    .filter(s -> s.getRemainStock() <= s.getWarningStock()).count();
            act.setWarningMsg(low > 0 ? "有" + low + "款商品库存低于预警值" : null);
            groupBuyActivityRepository.save(act);
        });
    }

    private PromoActivityVO toSeckillActivityVO(SeckillActivityEntity e) {
        PromoActivityVO vo = new PromoActivityVO();
        vo.setId(e.getActivityCode());
        vo.setTitle(e.getTitle());
        vo.setStartTime(PromotionSupport.formatTime(e.getStartTime()));
        vo.setEndTime(PromotionSupport.formatTime(e.getEndTime()));
        vo.setOnline(e.getOnline());
        vo.setStatus(PromotionSupport.resolveActivityStatus(e.getStartTime(), e.getEndTime(), e.getOnline()));
        vo.setWarning(e.getWarningMsg());
        return vo;
    }

    private PromoActivityVO toGroupActivityVO(GroupBuyActivityEntity e) {
        PromoActivityVO vo = new PromoActivityVO();
        vo.setId(e.getActivityCode());
        vo.setTitle(e.getTitle());
        vo.setStartTime(PromotionSupport.formatTime(e.getStartTime()));
        vo.setEndTime(PromotionSupport.formatTime(e.getEndTime()));
        vo.setOnline(e.getOnline());
        vo.setStatus(PromotionSupport.resolveActivityStatus(e.getStartTime(), e.getEndTime(), e.getOnline()));
        vo.setWarning(e.getWarningMsg());
        return vo;
    }

    private TimeSlotVO toTimeSlotVO(SeckillTimeSlotEntity s) {
        TimeSlotVO vo = new TimeSlotVO();
        vo.setId(s.getSlotCode());
        vo.setName(s.getSlotName());
        vo.setStart(s.getStartTime());
        vo.setEnd(s.getEndTime());
        vo.setEnabled(s.getEnabled());
        return vo;
    }

    private TimeSlotVO toGroupTimeSlotVO(GroupBuyTimeSlotEntity s) {
        TimeSlotVO vo = new TimeSlotVO();
        vo.setId(s.getSlotCode());
        vo.setName(s.getSlotName());
        vo.setStart(s.getStartTime());
        vo.setEnd(s.getEndTime());
        vo.setEnabled(s.getEnabled());
        return vo;
    }

    private SeckillSkuVO toSeckillSkuVO(SeckillSkuEntity s) {
        Product p = productRepository.findByProductNoAndDeletedFalse(s.getProductNo()).orElse(null);
        SeckillSkuVO vo = new SeckillSkuVO();
        vo.setId(String.valueOf(s.getId()));
        vo.setName(s.getProductName());
        vo.setProductCode(s.getProductNo());
        vo.setPrice(s.getPrice());
        vo.setSeckillPrice(s.getSeckillPrice());
        vo.setSeckillQty(s.getSeckillQty());
        vo.setRemainStock(s.getRemainStock());
        vo.setTotalStock(p == null ? s.getRemainStock() : p.getStock());
        vo.setWarningStock(s.getWarningStock());
        vo.setLimitQty(s.getLimitQty());
        vo.setSort(s.getSortNum());
        return vo;
    }

    private GroupBuySkuVO toGroupSkuVO(GroupBuySkuEntity s) {
        Product p = productRepository.findByProductNoAndDeletedFalse(s.getProductNo()).orElse(null);
        GroupBuySkuVO vo = new GroupBuySkuVO();
        vo.setId(String.valueOf(s.getId()));
        vo.setName(s.getProductName());
        vo.setProductCode(s.getProductNo());
        vo.setPrice(s.getPrice());
        vo.setGroupPrice(s.getGroupPrice());
        vo.setGroupSize(s.getGroupSize());
        vo.setGroupQty(s.getGroupQty());
        vo.setRemainStock(s.getRemainStock());
        vo.setTotalStock(p == null ? s.getRemainStock() : p.getStock());
        vo.setWarningStock(s.getWarningStock());
        vo.setLimitQty(s.getLimitQty());
        vo.setSort(s.getSortNum());
        if (s.getAttrsJson() != null) {
            try {
                vo.setAttrs(MAPPER.readValue(s.getAttrsJson(), new TypeReference<>() {}));
            } catch (Exception ignored) {
                vo.setAttrs(Map.of());
            }
        }
        return vo;
    }

    private CouponVO toCouponVO(CouponEntity c) {
        CouponVO vo = new CouponVO();
        vo.setId(c.getCouponCode());
        vo.setName(c.getCouponName());
        vo.setType(c.getCouponType());
        vo.setTypeLabel(PromotionSupport.couponTypeLabel(c.getCouponType()));
        vo.setProducts(scopeProductsText(c));
        vo.setThreshold(PromotionSupport.thresholdText(c.getThresholdAmount(), c.getFaceValue()));
        vo.setFaceValue(c.getFaceValue());
        vo.setIssueQty(c.getIssueQty() < 0 ? "不限量" : String.valueOf(c.getIssueQty()));
        vo.setClaimed(c.getClaimedQty());
        vo.setUsed(c.getUsedQty());
        vo.setPlatform(c.getPlatform() == null ? "全平台" : c.getPlatform());
        vo.setValidity(c.getValidityDays() + "天");
        vo.setTimeRange(PromotionSupport.formatTime(c.getStartTime()) + "至" + PromotionSupport.formatTime(c.getEndTime()));
        String status = resolveCouponStatus(c);
        vo.setStatus(status);
        vo.setStatusLabel(STATUS_LABEL.getOrDefault(status, status));
        vo.setOnline(c.getOnline());
        vo.setScopeType(c.getScopeType());
        return vo;
    }

    private String resolveCouponStatus(CouponEntity c) {
        if (!Boolean.TRUE.equals(c.getOnline())) {
            return "ended";
        }
        if (c.getStartTime() != null && LocalDateTime.now().isBefore(c.getStartTime())) {
            return "pending";
        }
        if (c.getEndTime() != null && LocalDateTime.now().isAfter(c.getEndTime())) {
            return "ended";
        }
        return "active";
    }

    private String scopeProductsText(CouponEntity c) {
        if ("all".equals(c.getScopeType())) {
            return "全部商品";
        }
        List<String> ids = Jsons.toStringList(c.getScopeJson());
        if ("product".equals(c.getScopeType())) {
            return ids.size() + "款";
        }
        if ("category".equals(c.getScopeType())) {
            return ids.size() + "个分类";
        }
        return "-";
    }

    private String buildScopeJson(CouponSaveRequest req) {
        if ("product".equals(req.getScopeType()) && req.getProductIds() != null) {
            return Jsons.toJson(req.getProductIds());
        }
        if ("category".equals(req.getScopeType()) && req.getCategoryIds() != null) {
            return Jsons.toJson(req.getCategoryIds());
        }
        return "[]";
    }

    private void parseScopeIds(CouponEntity c, CouponDetailVO vo) {
        List<String> ids = Jsons.toStringList(c.getScopeJson());
        if ("product".equals(c.getScopeType())) {
            vo.setProductIds(ids);
        } else if ("category".equals(c.getScopeType())) {
            vo.setCategoryIds(ids);
        }
    }

    private CouponHistoryVO toHistoryVO(CouponClaimEntity h) {
        CouponHistoryVO vo = new CouponHistoryVO();
        vo.setCouponId(h.getCouponCode());
        vo.setMember(h.getMemberPhone() + " (" + (h.getMemberNo() == null ? "-" : h.getMemberNo()) + ")");
        vo.setMethod(h.getClaimMethod());
        vo.setClaimTime(PromotionSupport.formatTime(h.getClaimTime()));
        vo.setStatus(h.getStatus());
        vo.setUseTime(h.getUseTime() == null ? "-" : PromotionSupport.formatTime(h.getUseTime()));
        vo.setOrderId(h.getOrderNo() == null ? "" : h.getOrderNo());
        return vo;
    }

    private Specification<SeckillActivityEntity> activitySpec(String keyword, LocalDate start, LocalDate end) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));
            if (keyword != null && !keyword.isBlank()) {
                String kw = "%" + keyword.trim() + "%";
                predicates.add(cb.or(cb.like(root.get("title"), kw), cb.like(root.get("activityCode"), kw)));
            }
            if (start != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startTime"), start.atStartOfDay()));
            }
            if (end != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("endTime"), end.atTime(LocalTime.MAX)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<GroupBuyActivityEntity> groupActivitySpec(String keyword, LocalDate start, LocalDate end) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));
            if (keyword != null && !keyword.isBlank()) {
                String kw = "%" + keyword.trim() + "%";
                predicates.add(cb.or(cb.like(root.get("title"), kw), cb.like(root.get("activityCode"), kw)));
            }
            if (start != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startTime"), start.atStartOfDay()));
            }
            if (end != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("endTime"), end.atTime(LocalTime.MAX)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<CouponEntity> couponSpec(String keyword, String type) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));
            if (keyword != null && !keyword.isBlank()) {
                String kw = "%" + keyword.trim() + "%";
                predicates.add(cb.or(cb.like(root.get("couponName"), kw), cb.like(root.get("couponCode"), kw)));
            }
            if (type != null && !type.isBlank()) {
                predicates.add(cb.equal(root.get("couponType"), type));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
