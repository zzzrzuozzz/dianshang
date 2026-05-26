package com.dianshang.admin.system.controller;

import com.dianshang.admin.common.ApiResponse;
import com.dianshang.admin.system.dto.MaintenanceStatusVO;
import com.dianshang.admin.system.service.MaintenanceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/system/maintenance")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    public MaintenanceController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @PostMapping("/clear-cache")
    public ApiResponse<Map<String, String>> clearCache() {
        maintenanceService.clearCache();
        return ApiResponse.ok(Map.of("message", "全局业务缓存已清除", "clearedAt",
                maintenanceService.buildInitializerStatus().getCacheClearedAt()));
    }

    @GetMapping("/initializer-status")
    public ApiResponse<MaintenanceStatusVO> initializerStatus() {
        return ApiResponse.ok(maintenanceService.buildInitializerStatus());
    }
}
