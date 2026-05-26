package com.dianshang.admin.order.controller;

import com.dianshang.admin.common.ApiResponse;
import com.dianshang.admin.common.PageResult;
import com.dianshang.admin.order.dto.*;
import com.dianshang.admin.order.service.AfterSaleService;
import com.dianshang.admin.order.service.ExpressTemplateService;
import com.dianshang.admin.order.service.OrderAddressService;
import com.dianshang.admin.order.service.OrderService;
import com.dianshang.admin.order.service.ReturnReasonService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;
    private final AfterSaleService afterSaleService;
    private final ReturnReasonService returnReasonService;
    private final ExpressTemplateService expressTemplateService;
    private final OrderAddressService orderAddressService;

    public OrderController(OrderService orderService,
                           AfterSaleService afterSaleService,
                           ReturnReasonService returnReasonService,
                           ExpressTemplateService expressTemplateService,
                           OrderAddressService orderAddressService) {
        this.orderService = orderService;
        this.afterSaleService = afterSaleService;
        this.returnReasonService = returnReasonService;
        this.expressTemplateService = expressTemplateService;
        this.orderAddressService = orderAddressService;
    }

    @GetMapping("/list")
    public ApiResponse<OrderPageVO> list(
            @RequestParam(required = false) String product,
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) String logisticsNo,
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "create") String timeType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "list") String pageType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.ok(orderService.list(product, orderId, logisticsNo, phone,
                timeType, startDate, endDate, status, pageType, page, pageSize));
    }

    @GetMapping("/detail/{orderNo}")
    public ApiResponse<OrderDetailVO> detail(@PathVariable String orderNo) {
        return ApiResponse.ok(orderService.detail(orderNo));
    }

    @PostMapping("/{orderNo}/ship")
    public ApiResponse<Void> ship(@PathVariable String orderNo, @RequestBody(required = false) ShipRequest request) {
        orderService.ship(orderNo, request);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{orderNo}/refund")
    public ApiResponse<Void> refund(@PathVariable String orderNo) {
        orderService.refund(orderNo);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{orderNo}/confirm")
    public ApiResponse<Void> confirm(@PathVariable String orderNo) {
        orderService.confirm(orderNo);
        return ApiResponse.ok(null);
    }

    @PostMapping("/batch/confirm")
    public ApiResponse<Void> batchConfirm(@Valid @RequestBody IdsRequest request) {
        orderService.batchConfirm(request.getIds());
        return ApiResponse.ok(null);
    }

    @GetMapping("/after-sale/list")
    public ApiResponse<AfterSalePageVO> afterSaleList(
            @RequestParam(required = false) String product,
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) String logisticsNo,
            @RequestParam(required = false) String afterSaleId,
            @RequestParam(defaultValue = "create") String timeType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "platform_pending") String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.ok(afterSaleService.list(product, orderId, logisticsNo, afterSaleId,
                timeType, startDate, endDate, status, page, pageSize));
    }

    @GetMapping("/setting/return-reason/list")
    public ApiResponse<PageResult<ReturnReasonVO>> returnReasonList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.ok(returnReasonService.list(keyword, page, pageSize));
    }

    @PostMapping("/setting/return-reason")
    public ApiResponse<ReturnReasonVO> createReturnReason(@Valid @RequestBody ReturnReasonSaveRequest request) {
        return ApiResponse.ok(returnReasonService.create(request));
    }

    @PutMapping("/setting/return-reason/{code}")
    public ApiResponse<ReturnReasonVO> updateReturnReason(@PathVariable String code,
                                                          @Valid @RequestBody ReturnReasonSaveRequest request) {
        return ApiResponse.ok(returnReasonService.update(code, request));
    }

    @DeleteMapping("/setting/return-reason/{code}")
    public ApiResponse<Void> deleteReturnReason(@PathVariable String code) {
        returnReasonService.delete(code);
        return ApiResponse.ok(null);
    }

    @PutMapping("/setting/return-reason/{code}/visible")
    public ApiResponse<Void> returnReasonVisible(@PathVariable String code, @RequestBody VisibleRequest request) {
        returnReasonService.updateVisible(code, Boolean.TRUE.equals(request.getVisible()));
        return ApiResponse.ok(null);
    }

    @GetMapping("/setting/express-template/list")
    public ApiResponse<PageResult<ExpressTemplateVO>> expressTemplateList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.ok(expressTemplateService.list(keyword, page, pageSize));
    }

    @PostMapping("/setting/express-template")
    public ApiResponse<ExpressTemplateVO> createExpressTemplate(
            @Valid @RequestBody ExpressTemplateSaveRequest request) {
        return ApiResponse.ok(expressTemplateService.create(request));
    }

    @PutMapping("/setting/express-template/{code}")
    public ApiResponse<ExpressTemplateVO> updateExpressTemplate(@PathVariable String code,
                                                              @Valid @RequestBody ExpressTemplateSaveRequest request) {
        return ApiResponse.ok(expressTemplateService.update(code, request));
    }

    @DeleteMapping("/setting/express-template/{code}")
    public ApiResponse<Void> deleteExpressTemplate(@PathVariable String code) {
        expressTemplateService.delete(code);
        return ApiResponse.ok(null);
    }

    @PutMapping("/setting/express-template/{code}/visible")
    public ApiResponse<Void> expressTemplateVisible(@PathVariable String code, @RequestBody VisibleRequest request) {
        expressTemplateService.updateVisible(code, Boolean.TRUE.equals(request.getVisible()));
        return ApiResponse.ok(null);
    }

    @PutMapping("/setting/express-template/{code}/default")
    public ApiResponse<Void> expressTemplateDefault(@PathVariable String code, @RequestBody DefaultRequest request) {
        expressTemplateService.updateDefault(code, Boolean.TRUE.equals(request.getIsDefault()));
        return ApiResponse.ok(null);
    }

    @GetMapping("/setting/address/list")
    public ApiResponse<PageResult<OrderAddressVO>> addressList(
            @RequestParam String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.ok(orderAddressService.list(type, keyword, page, pageSize));
    }

    @PostMapping("/setting/address")
    public ApiResponse<OrderAddressVO> createAddress(@Valid @RequestBody OrderAddressSaveRequest request) {
        return ApiResponse.ok(orderAddressService.create(request));
    }

    @PutMapping("/setting/address/{code}")
    public ApiResponse<OrderAddressVO> updateAddress(@PathVariable String code,
                                                     @Valid @RequestBody OrderAddressSaveRequest request) {
        return ApiResponse.ok(orderAddressService.update(code, request));
    }

    @DeleteMapping("/setting/address/{code}")
    public ApiResponse<Void> deleteAddress(@PathVariable String code) {
        orderAddressService.delete(code);
        return ApiResponse.ok(null);
    }

    @PutMapping("/setting/address/{code}/visible")
    public ApiResponse<Void> addressVisible(@PathVariable String code, @RequestBody VisibleRequest request) {
        orderAddressService.updateVisible(code, Boolean.TRUE.equals(request.getVisible()));
        return ApiResponse.ok(null);
    }

    @PutMapping("/setting/address/{code}/default")
    public ApiResponse<Void> addressDefault(@PathVariable String code, @RequestBody DefaultRequest request) {
        orderAddressService.updateDefault(code, Boolean.TRUE.equals(request.getIsDefault()));
        return ApiResponse.ok(null);
    }
}
