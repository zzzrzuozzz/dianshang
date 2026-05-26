package com.dianshang.admin.permission.controller;

import com.dianshang.admin.common.ApiResponse;
import com.dianshang.admin.common.PageResult;
import com.dianshang.admin.permission.dto.AdminPageRequest;
import com.dianshang.admin.permission.dto.AdminSaveRequest;
import com.dianshang.admin.permission.dto.AdminVO;
import com.dianshang.admin.permission.service.AdminPermissionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/system/admin")
public class SysAdminPermissionController {

    private final AdminPermissionService adminPermissionService;

    public SysAdminPermissionController(AdminPermissionService adminPermissionService) {
        this.adminPermissionService = adminPermissionService;
    }

    @PostMapping("/page")
    public ApiResponse<PageResult<AdminVO>> page(@RequestBody AdminPageRequest request) {
        return ApiResponse.ok(adminPermissionService.page(request));
    }

    @PostMapping("/save")
    public ApiResponse<Void> save(@Valid @RequestBody AdminSaveRequest request) {
        adminPermissionService.save(request);
        return ApiResponse.ok(null);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        adminPermissionService.updateStatus(id, body.get("status"));
        return ApiResponse.ok(null);
    }
}
