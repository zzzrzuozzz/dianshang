package com.dianshang.admin.system.controller;

import com.dianshang.admin.common.ApiResponse;
import com.dianshang.admin.system.dto.PlatformConfigVO;
import com.dianshang.admin.system.dto.SysConfigUpdateItem;
import com.dianshang.admin.system.dto.SysConfigVO;
import com.dianshang.admin.system.service.SysConfigService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system/config")
public class SysConfigController {

    private final SysConfigService sysConfigService;

    public SysConfigController(SysConfigService sysConfigService) {
        this.sysConfigService = sysConfigService;
    }

    /** 登录页/侧栏等无需鉴权的公开配置 */
    @GetMapping("/public")
    public ApiResponse<Map<String, String>> publicConfig() {
        return ApiResponse.ok(sysConfigService.publicMap());
    }

    @GetMapping("/get-all")
    public ApiResponse<List<SysConfigVO>> getAll() {
        return ApiResponse.ok(sysConfigService.listAll());
    }

    /** 平台基础信息（表单结构化，与前端 setting/index 联调） */
    @GetMapping("/platform")
    public ApiResponse<PlatformConfigVO> platform() {
        return ApiResponse.ok(sysConfigService.getPlatformConfig());
    }

    @PutMapping("/platform")
    public ApiResponse<Void> savePlatform(@Valid @RequestBody PlatformConfigVO config) {
        sysConfigService.savePlatformConfig(config);
        return ApiResponse.ok(null);
    }

    @PutMapping("/update-batch")
    public ApiResponse<Void> updateBatch(@Valid @RequestBody List<SysConfigUpdateItem> configs) {
        sysConfigService.updateBatch(configs);
        return ApiResponse.ok(null);
    }
}
