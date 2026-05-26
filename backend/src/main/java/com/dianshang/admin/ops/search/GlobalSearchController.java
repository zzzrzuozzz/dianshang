package com.dianshang.admin.ops.search;

import com.dianshang.admin.common.ApiResponse;
import com.dianshang.admin.ops.search.dto.GlobalSearchResultVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ops/global")
public class GlobalSearchController {

    private final GlobalSearchService globalSearchService;

    public GlobalSearchController(GlobalSearchService globalSearchService) {
        this.globalSearchService = globalSearchService;
    }

    @GetMapping("/search")
    public ApiResponse<GlobalSearchResultVO> search(@RequestParam String keyword) {
        return ApiResponse.ok(globalSearchService.search(keyword));
    }
}
