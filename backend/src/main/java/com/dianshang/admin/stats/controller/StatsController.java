package com.dianshang.admin.stats.controller;

import com.dianshang.admin.common.ApiResponse;
import com.dianshang.admin.stats.dto.FlowReportVO;
import com.dianshang.admin.stats.dto.ProductCategoryPageVO;
import com.dianshang.admin.stats.dto.ProductRankingPageVO;
import com.dianshang.admin.stats.dto.TransactionOverviewVO;
import com.dianshang.admin.stats.service.StatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/transaction/overview")
    public ApiResponse<TransactionOverviewVO> transactionOverview(
            @RequestParam(defaultValue = "week") String range,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ApiResponse.ok(statsService.transactionOverview(range, startDate, endDate));
    }

    @GetMapping("/flow/report")
    public ApiResponse<FlowReportVO> flowReport(
            @RequestParam(defaultValue = "week") String range,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ApiResponse.ok(statsService.flowReport(range, startDate, endDate));
    }

    @GetMapping("/product/category")
    public ApiResponse<ProductCategoryPageVO> productCategory(
            @RequestParam(defaultValue = "week") String range,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.ok(statsService.productCategory(range, startDate, endDate, page, pageSize));
    }

    @GetMapping("/product/ranking")
    public ApiResponse<ProductRankingPageVO> productRanking(
            @RequestParam(defaultValue = "week") String range,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.ok(statsService.productRanking(range, startDate, endDate, page, pageSize));
    }
}
