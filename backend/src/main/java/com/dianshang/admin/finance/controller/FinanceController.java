package com.dianshang.admin.finance.controller;

import com.dianshang.admin.common.ApiResponse;
import com.dianshang.admin.common.PageResult;
import com.dianshang.admin.finance.dto.*;
import com.dianshang.admin.finance.service.FinanceService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/finance")
public class FinanceController {

    private final FinanceService financeService;

    public FinanceController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @GetMapping("/overview")
    public ApiResponse<FinanceOverviewVO> overview(
            @RequestParam(defaultValue = "day") String granularity) {
        return ApiResponse.ok(financeService.overview(granularity));
    }

    @PostMapping("/statement/page")
    public ApiResponse<PageResult<TransactionRecordVO>> statementPage(
            @RequestBody StatementPageRequest request) {
        return ApiResponse.ok(financeService.statementPage(request));
    }

    @PostMapping("/statement/export")
    public void exportStatement(@RequestBody StatementPageRequest request,
                              HttpServletResponse response) throws Exception {
        financeService.exportStatement(request, response);
    }

    @PostMapping("/withdraw/page")
    public ApiResponse<PageResult<WithdrawApplyVO>> withdrawPage(
            @RequestBody WithdrawPageRequest request) {
        return ApiResponse.ok(financeService.withdrawPage(request));
    }

    @PutMapping("/withdraw/verify")
    public ApiResponse<Void> verifyWithdraw(@Valid @RequestBody WithdrawVerifyRequest request,
                                            Authentication authentication) {
        String operator = authentication != null ? authentication.getName() : "admin";
        financeService.verifyWithdraw(request, operator);
        return ApiResponse.ok(null);
    }

    /** 全库对账：补全订单流水并同步首页看板指标 */
    @PostMapping("/reconcile")
    public ApiResponse<java.util.Map<String, Object>> reconcile() {
        return ApiResponse.ok(financeService.reconcileGlobal());
    }
}
