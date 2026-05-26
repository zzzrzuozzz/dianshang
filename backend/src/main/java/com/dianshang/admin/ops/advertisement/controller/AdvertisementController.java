package com.dianshang.admin.ops.advertisement.controller;

import com.dianshang.admin.common.ApiResponse;
import com.dianshang.admin.ops.advertisement.dto.*;
import com.dianshang.admin.ops.advertisement.service.AdvertisementService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/ops/advertisement")
public class AdvertisementController {

    private final AdvertisementService advertisementService;

    public AdvertisementController(AdvertisementService advertisementService) {
        this.advertisementService = advertisementService;
    }

    @GetMapping("/list")
    public ApiResponse<AdvertisementPageVO> list(
            @RequestParam(defaultValue = "all") String tab,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.ok(advertisementService.list(tab, title, startDate, endDate, page, pageSize));
    }

    @GetMapping("/{advCode}")
    public ApiResponse<AdvertisementDetailVO> detail(@PathVariable String advCode) {
        return ApiResponse.ok(advertisementService.detail(advCode));
    }

    @PostMapping("/save")
    public ApiResponse<String> save(@Valid @RequestBody AdvertisementSaveRequest request) {
        return ApiResponse.ok(advertisementService.save(request));
    }

    @PostMapping("/{advCode}/online")
    public ApiResponse<Void> online(@PathVariable String advCode, @RequestBody Map<String, Boolean> body) {
        advertisementService.toggleOnline(advCode, Boolean.TRUE.equals(body.get("online")));
        return ApiResponse.ok(null);
    }

    @PostMapping("/{advCode}/pin")
    public ApiResponse<Void> pin(@PathVariable String advCode) {
        advertisementService.pin(advCode);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{advCode}")
    public ApiResponse<Void> delete(@PathVariable String advCode) {
        advertisementService.delete(advCode);
        return ApiResponse.ok(null);
    }
}
