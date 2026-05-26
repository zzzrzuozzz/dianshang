package com.dianshang.admin.promotion.controller;

import com.dianshang.admin.common.ApiResponse;
import com.dianshang.admin.promotion.dto.*;
import com.dianshang.admin.promotion.service.PromotionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/promotion")
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @GetMapping("/products/picker")
    public ApiResponse<List<ProductPickerVO>> productPicker(@RequestParam(required = false) String keyword) {
        return ApiResponse.ok(promotionService.productPicker(keyword));
    }

    // ---------- Seckill ----------

    @GetMapping("/seckill/list")
    public ApiResponse<PromoActivityPageVO> seckillList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "all") String tab,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.ok(promotionService.seckillList(keyword, tab, startDate, endDate, page, pageSize));
    }

    @PostMapping("/seckill/save")
    public ApiResponse<String> saveSeckill(@Valid @RequestBody PromoActivitySaveRequest request) {
        return ApiResponse.ok(promotionService.saveSeckill(request));
    }

    @PostMapping("/seckill/{activityCode}/online")
    public ApiResponse<Void> seckillOnline(@PathVariable String activityCode, @RequestBody Map<String, Boolean> body) {
        promotionService.toggleSeckillOnline(activityCode, Boolean.TRUE.equals(body.get("online")));
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/seckill/{activityCode}")
    public ApiResponse<Void> deleteSeckill(@PathVariable String activityCode) {
        promotionService.deleteSeckill(activityCode);
        return ApiResponse.ok(null);
    }

    @GetMapping("/seckill/time/list")
    public ApiResponse<List<TimeSlotVO>> seckillTimeList() {
        return ApiResponse.ok(promotionService.seckillTimeList());
    }

    @PostMapping("/seckill/time/save")
    public ApiResponse<String> saveSeckillTime(@Valid @RequestBody TimeSlotSaveRequest request) {
        return ApiResponse.ok(promotionService.saveSeckillTime(request));
    }

    @PostMapping("/seckill/time/{slotCode}/enabled")
    public ApiResponse<Void> seckillTimeEnabled(@PathVariable String slotCode, @RequestBody Map<String, Boolean> body) {
        promotionService.toggleSeckillTime(slotCode, Boolean.TRUE.equals(body.get("enabled")));
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/seckill/time/{slotCode}")
    public ApiResponse<Void> deleteSeckillTime(@PathVariable String slotCode) {
        promotionService.deleteSeckillTime(slotCode);
        return ApiResponse.ok(null);
    }

    @GetMapping("/seckill/{activityCode}/slots")
    public ApiResponse<List<TimeSlotVO>> seckillActivitySlots(@PathVariable String activityCode) {
        return ApiResponse.ok(promotionService.seckillActivitySlots(activityCode));
    }

    @GetMapping("/seckill/{activityCode}/sku/{slotCode}")
    public ApiResponse<List<SeckillSkuVO>> seckillSkuList(@PathVariable String activityCode,
                                                          @PathVariable String slotCode) {
        return ApiResponse.ok(promotionService.seckillSkuList(activityCode, slotCode));
    }

    @PostMapping("/seckill/sku/save")
    public ApiResponse<Void> saveSeckillSku(@Valid @RequestBody SeckillSkuSaveRequest request) {
        promotionService.saveSeckillSku(request);
        return ApiResponse.ok(null);
    }

    // ---------- Group Buy ----------

    @GetMapping("/group-buy/list")
    public ApiResponse<PromoActivityPageVO> groupBuyList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "all") String tab,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.ok(promotionService.groupBuyList(keyword, tab, startDate, endDate, page, pageSize));
    }

    @PostMapping("/group-buy/save")
    public ApiResponse<String> saveGroupBuy(@Valid @RequestBody PromoActivitySaveRequest request) {
        return ApiResponse.ok(promotionService.saveGroupBuy(request));
    }

    @PostMapping("/group-buy/{activityCode}/online")
    public ApiResponse<Void> groupBuyOnline(@PathVariable String activityCode, @RequestBody Map<String, Boolean> body) {
        promotionService.toggleGroupBuyOnline(activityCode, Boolean.TRUE.equals(body.get("online")));
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/group-buy/{activityCode}")
    public ApiResponse<Void> deleteGroupBuy(@PathVariable String activityCode) {
        promotionService.deleteGroupBuy(activityCode);
        return ApiResponse.ok(null);
    }

    @GetMapping("/group-buy/{activityCode}/time/list")
    public ApiResponse<List<TimeSlotVO>> groupBuyTimeList(@PathVariable String activityCode) {
        return ApiResponse.ok(promotionService.groupBuyTimeList(activityCode));
    }

    @PostMapping("/group-buy/time/save")
    public ApiResponse<String> saveGroupBuyTime(@Valid @RequestBody TimeSlotSaveRequest request) {
        return ApiResponse.ok(promotionService.saveGroupBuyTime(request));
    }

    @PostMapping("/group-buy/{activityCode}/time/{slotCode}/enabled")
    public ApiResponse<Void> groupBuyTimeEnabled(@PathVariable String activityCode,
                                                 @PathVariable String slotCode,
                                                 @RequestBody Map<String, Boolean> body) {
        promotionService.toggleGroupBuyTime(activityCode, slotCode, Boolean.TRUE.equals(body.get("enabled")));
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/group-buy/{activityCode}/time/{slotCode}")
    public ApiResponse<Void> deleteGroupBuyTime(@PathVariable String activityCode, @PathVariable String slotCode) {
        promotionService.deleteGroupBuyTime(activityCode, slotCode);
        return ApiResponse.ok(null);
    }

    @GetMapping("/group-buy/{activityCode}/sku/{slotCode}")
    public ApiResponse<List<GroupBuySkuVO>> groupBuySkuList(@PathVariable String activityCode,
                                                            @PathVariable String slotCode) {
        return ApiResponse.ok(promotionService.groupBuySkuList(activityCode, slotCode));
    }

    @PostMapping("/group-buy/sku/save")
    public ApiResponse<Void> saveGroupBuySku(@Valid @RequestBody GroupBuySkuSaveRequest request) {
        promotionService.saveGroupBuySku(request);
        return ApiResponse.ok(null);
    }

    // ---------- Coupon ----------

    @GetMapping("/coupon/list")
    public ApiResponse<CouponPageVO> couponList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "all") String tab,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.ok(promotionService.couponList(keyword, type, tab, page, pageSize));
    }

    @GetMapping("/coupon/{couponCode}")
    public ApiResponse<CouponDetailVO> couponDetail(@PathVariable String couponCode) {
        return ApiResponse.ok(promotionService.couponDetail(couponCode));
    }

    @PostMapping("/coupon/save")
    public ApiResponse<String> saveCoupon(@Valid @RequestBody CouponSaveRequest request) {
        return ApiResponse.ok(promotionService.saveCoupon(request));
    }

    @PostMapping("/coupon/{couponCode}/online")
    public ApiResponse<Void> couponOnline(@PathVariable String couponCode, @RequestBody Map<String, Boolean> body) {
        promotionService.toggleCouponOnline(couponCode, Boolean.TRUE.equals(body.get("online")));
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/coupon/{couponCode}")
    public ApiResponse<Void> deleteCoupon(@PathVariable String couponCode) {
        promotionService.deleteCoupon(couponCode);
        return ApiResponse.ok(null);
    }

    @GetMapping("/coupon/{couponCode}/history")
    public ApiResponse<List<CouponHistoryVO>> couponHistory(
            @PathVariable String couponCode,
            @RequestParam(defaultValue = "all") String tab,
            @RequestParam(required = false) String member,
            @RequestParam(required = false) String orderId) {
        return ApiResponse.ok(promotionService.couponHistory(couponCode, tab, member, orderId));
    }
}
