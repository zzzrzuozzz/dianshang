package com.dianshang.admin.ops.notification.controller;

import com.dianshang.admin.common.ApiResponse;
import com.dianshang.admin.ops.notification.dto.*;
import com.dianshang.admin.ops.notification.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/ops/notification")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/list")
    public ApiResponse<NotificationPageVO> list(
            @RequestParam String msgType,
            @RequestParam(defaultValue = "all") String tab,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.ok(notificationService.list(msgType, tab, title, startDate, endDate, page, pageSize));
    }

    @GetMapping("/{notifyCode}")
    public ApiResponse<NotificationDetailVO> detail(@PathVariable String notifyCode) {
        return ApiResponse.ok(notificationService.detail(notifyCode));
    }

    @PostMapping("/save")
    public ApiResponse<String> save(@Valid @RequestBody NotificationSaveRequest request) {
        return ApiResponse.ok(notificationService.save(request));
    }

    @PostMapping("/estimate")
    public ApiResponse<Map<String, Integer>> estimate(@RequestBody AudienceEstimateRequest request) {
        int count = notificationService.estimate(request);
        return ApiResponse.ok(Map.of("estimatedUsers", count));
    }

    @PostMapping("/{notifyCode}/resend")
    public ApiResponse<Void> resend(@PathVariable String notifyCode) {
        notificationService.resend(notifyCode);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{notifyCode}")
    public ApiResponse<Void> delete(@PathVariable String notifyCode) {
        notificationService.delete(notifyCode);
        return ApiResponse.ok(null);
    }

    @PostMapping("/batch/delete")
    public ApiResponse<Map<String, Integer>> batchDelete(@Valid @RequestBody NotifyCodesRequest request) {
        int count = notificationService.batchDelete(request.getIds());
        return ApiResponse.ok(Map.of("count", count));
    }

    @PostMapping("/batch/resend")
    public ApiResponse<Map<String, Integer>> batchResend(@Valid @RequestBody NotifyCodesRequest request) {
        int count = notificationService.batchResend(request.getIds());
        return ApiResponse.ok(Map.of("count", count));
    }
}
