package com.dianshang.admin.config;

import com.dianshang.admin.promotion.entity.*;
import com.dianshang.admin.promotion.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class PromotionDataInitializer implements CommandLineRunner {

    private final SeckillActivityRepository seckillActivityRepository;
    private final SeckillTimeSlotRepository seckillTimeSlotRepository;
    private final SeckillSkuRepository seckillSkuRepository;
    private final GroupBuyActivityRepository groupBuyActivityRepository;
    private final GroupBuyTimeSlotRepository groupBuyTimeSlotRepository;
    private final CouponRepository couponRepository;
    private final CouponClaimRepository couponClaimRepository;

    public PromotionDataInitializer(SeckillActivityRepository seckillActivityRepository,
                                    SeckillTimeSlotRepository seckillTimeSlotRepository,
                                    SeckillSkuRepository seckillSkuRepository,
                                    GroupBuyActivityRepository groupBuyActivityRepository,
                                    GroupBuyTimeSlotRepository groupBuyTimeSlotRepository,
                                    CouponRepository couponRepository,
                                    CouponClaimRepository couponClaimRepository) {
        this.seckillActivityRepository = seckillActivityRepository;
        this.seckillTimeSlotRepository = seckillTimeSlotRepository;
        this.seckillSkuRepository = seckillSkuRepository;
        this.groupBuyActivityRepository = groupBuyActivityRepository;
        this.groupBuyTimeSlotRepository = groupBuyTimeSlotRepository;
        this.couponRepository = couponRepository;
        this.couponClaimRepository = couponClaimRepository;
    }

    @Override
    public void run(String... args) {
        if (seckillActivityRepository.count() > 0) {
            return;
        }
        seedSeckill();
        seedGroupBuy();
        seedCoupons();
    }

    private void seedSeckill() {
        saveSeckillAct("BZ6542", "春季家电家具疯狂秒杀",
                LocalDateTime.of(2024, 8, 8, 16, 14), LocalDateTime.of(2024, 8, 18, 16, 14), true, true);
        saveSeckillAct("BZ6543", "夏日清凉专场秒杀",
                LocalDateTime.of(2024, 8, 1, 10, 0), LocalDateTime.of(2024, 8, 31, 22, 0), true, false);
        saveSeckillAct("BZ6544", "双十一预热秒杀",
                LocalDateTime.of(2024, 7, 1, 0, 0), LocalDateTime.of(2024, 7, 31, 23, 59), false, false);
        saveSeckillAct("BZ6545", "国庆特惠秒杀",
                LocalDateTime.of(2024, 6, 1, 8, 0), LocalDateTime.of(2024, 6, 7, 23, 59), false, false);

        saveTime("BZ6542", "秒杀8点", "08:00:00", "10:00:00", true);
        saveTime("BZ6543", "秒杀10点", "10:00:01", "12:00:00", true);
        saveTime("BZ6544", "秒杀12点", "12:00:01", "14:00:00", true);
        saveTime("BZ6545", "秒杀14点", "14:00:01", "16:00:00", true);

        SeckillSkuEntity sku = new SeckillSkuEntity();
        sku.setActivityCode("BZ6542");
        sku.setSlotCode("BZ6542");
        sku.setProductNo("025342");
        sku.setProductName("100角磨机配件手动自锁压板神箭手");
        sku.setPrice(BigDecimal.valueOf(12));
        sku.setSeckillPrice(BigDecimal.TEN);
        sku.setSeckillQty(500);
        sku.setRemainStock(480);
        sku.setWarningStock(500);
        sku.setLimitQty(2);
        sku.setSortNum(1);
        seckillSkuRepository.save(sku);
        seckillActivityRepository.findByActivityCodeAndDeletedFalse("BZ6542").ifPresent(a -> {
            a.setWarningMsg("有1款商品库存低于预警值");
            seckillActivityRepository.save(a);
        });
    }

    private void saveSeckillAct(String code, String title, LocalDateTime start, LocalDateTime end,
                                boolean online, boolean warn) {
        SeckillActivityEntity e = new SeckillActivityEntity();
        e.setActivityCode(code);
        e.setTitle(title);
        e.setStartTime(start);
        e.setEndTime(end);
        e.setOnline(online);
        if (warn) {
            e.setWarningMsg("有1款商品库存低于预警值");
        }
        seckillActivityRepository.save(e);
    }

    private void saveTime(String code, String name, String start, String end, boolean enabled) {
        SeckillTimeSlotEntity s = new SeckillTimeSlotEntity();
        s.setSlotCode(code);
        s.setSlotName(name);
        s.setStartTime(start);
        s.setEndTime(end);
        s.setEnabled(enabled);
        seckillTimeSlotRepository.save(s);
    }

    private void seedGroupBuy() {
        GroupBuyActivityEntity e = new GroupBuyActivityEntity();
        e.setActivityCode("GB6542");
        e.setTitle("夏季家电家具疯狂团购");
        e.setStartTime(LocalDateTime.of(2024, 8, 8, 16, 14));
        e.setEndTime(LocalDateTime.of(2024, 8, 18, 16, 14));
        e.setOnline(true);
        e.setWarningMsg("有1款商品库存低于预警值");
        groupBuyActivityRepository.save(e);

        GroupBuyActivityEntity e2 = new GroupBuyActivityEntity();
        e2.setActivityCode("GB6543");
        e2.setTitle("母婴用品拼团专场");
        e2.setStartTime(LocalDateTime.of(2024, 8, 1, 10, 0));
        e2.setEndTime(LocalDateTime.of(2024, 8, 31, 22, 0));
        e2.setOnline(true);
        groupBuyActivityRepository.save(e2);

        GroupBuyTimeSlotEntity slot = new GroupBuyTimeSlotEntity();
        slot.setActivityCode("GB6542");
        slot.setSlotCode("GT001");
        slot.setSlotName("上午场");
        slot.setStartTime("08:00:00");
        slot.setEndTime("12:00:00");
        slot.setEnabled(true);
        groupBuyTimeSlotRepository.save(slot);
    }

    private void seedCoupons() {
        CouponEntity c1 = new CouponEntity();
        c1.setCouponCode("BZ6542");
        c1.setCouponName("全品类通用券");
        c1.setCouponType("newcomer");
        c1.setScopeType("all");
        c1.setScopeJson("[]");
        c1.setThresholdAmount(BigDecimal.valueOf(20));
        c1.setFaceValue(BigDecimal.TEN);
        c1.setIssueQty(-1);
        c1.setClaimedQty(1000);
        c1.setUsedQty(500);
        c1.setPlatform("APP");
        c1.setValidityDays(15);
        c1.setStartTime(LocalDateTime.of(2024, 8, 10, 0, 0));
        c1.setEndTime(LocalDateTime.of(2024, 10, 1, 23, 59));
        c1.setOnline(true);
        couponRepository.save(c1);

        CouponEntity c2 = new CouponEntity();
        c2.setCouponCode("BZ6543");
        c2.setCouponName("满100减30券");
        c2.setCouponType("shopping");
        c2.setScopeType("product");
        c2.setScopeJson("[\"025342\",\"025343\"]");
        c2.setThresholdAmount(BigDecimal.valueOf(100));
        c2.setFaceValue(BigDecimal.valueOf(30));
        c2.setIssueQty(1000);
        c2.setClaimedQty(800);
        c2.setUsedQty(400);
        c2.setPlatform("小程序");
        c2.setValidityDays(30);
        c2.setStartTime(LocalDateTime.of(2024, 8, 1, 0, 0));
        c2.setEndTime(LocalDateTime.of(2024, 9, 30, 23, 59));
        c2.setOnline(true);
        couponRepository.save(c2);

        saveClaim("BZ6542", "13088888888", "BZ6542", "主动领取", "pending", null);
        saveClaim("BZ6542", "13800138000", "BZ6543", "系统赠送", "used", "69265555522");
        saveClaim("BZ6542", "13900139000", "BZ6544", "主动领取", "expired", null);
    }

    private void saveClaim(String couponCode, String phone, String memberNo, String method,
                           String status, String orderNo) {
        CouponClaimEntity h = new CouponClaimEntity();
        h.setCouponCode(couponCode);
        h.setMemberPhone(phone);
        h.setMemberNo(memberNo);
        h.setClaimMethod(method);
        h.setClaimTime(LocalDateTime.of(2024, 8, 10, 10, 0));
        h.setStatus(status);
        if ("used".equals(status)) {
            h.setUseTime(LocalDateTime.of(2024, 8, 12, 16, 30));
            h.setOrderNo(orderNo);
        }
        if ("expired".equals(status)) {
            h.setUseTime(LocalDateTime.of(2024, 8, 20, 23, 59));
        }
        couponClaimRepository.save(h);
    }
}
