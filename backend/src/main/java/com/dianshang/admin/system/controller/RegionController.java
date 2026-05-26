package com.dianshang.admin.system.controller;

import com.dianshang.admin.common.ApiResponse;
import com.dianshang.admin.system.dto.RegionVO;
import com.dianshang.admin.system.service.RegionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/system/region")
public class RegionController {

    private final RegionService regionService;

    public RegionController(RegionService regionService) {
        this.regionService = regionService;
    }

    /**
     * 省市区级联懒加载：根据父级代码获取直接下级区划。
     */
    @GetMapping("/list-by-parent")
    public ApiResponse<List<RegionVO>> listByParent(
            @RequestParam(defaultValue = "0") String parentCode) {
        return ApiResponse.ok(regionService.listByParent(parentCode));
    }
}
