package com.dianshang.admin.permission.controller;

import com.dianshang.admin.common.ApiResponse;
import com.dianshang.admin.permission.dto.MenuSaveRequest;
import com.dianshang.admin.permission.dto.MenuTreeVO;
import com.dianshang.admin.permission.service.MenuService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system/menu")
public class SysMenuController {

    private final MenuService menuService;

    public SysMenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/tree")
    public ApiResponse<List<MenuTreeVO>> tree() {
        return ApiResponse.ok(menuService.tree());
    }

    @PostMapping("/save")
    public ApiResponse<Void> save(@Valid @RequestBody MenuSaveRequest request) {
        menuService.save(request);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return ApiResponse.ok(null);
    }
}
