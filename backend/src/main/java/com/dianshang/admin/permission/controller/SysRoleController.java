package com.dianshang.admin.permission.controller;

import com.dianshang.admin.common.ApiResponse;
import com.dianshang.admin.common.PageResult;
import com.dianshang.admin.permission.dto.*;
import com.dianshang.admin.permission.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system/role")
public class SysRoleController {

    private final RoleService roleService;

    public SysRoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping("/page")
    public ApiResponse<PageResult<RoleVO>> page(@RequestBody RolePageRequest request) {
        return ApiResponse.ok(roleService.page(request));
    }

    @GetMapping("/options")
    public ApiResponse<List<RoleOptionVO>> options() {
        return ApiResponse.ok(roleService.options());
    }

    @GetMapping("/{id}/menu-ids")
    public ApiResponse<List<Long>> menuIds(@PathVariable Long id) {
        return ApiResponse.ok(roleService.menuIdsByRole(id));
    }

    @PostMapping("/save")
    public ApiResponse<Void> save(@Valid @RequestBody RoleSaveRequest request) {
        roleService.save(request);
        return ApiResponse.ok(null);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        roleService.updateStatus(id, body.get("status"));
        return ApiResponse.ok(null);
    }

    @PostMapping("/save-permissions")
    public ApiResponse<Void> savePermissions(@Valid @RequestBody RolePermissionSaveRequest request) {
        roleService.savePermissions(request);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ApiResponse.ok(null);
    }
}
