package com.dianshang.admin.ops.controller;

import com.dianshang.admin.common.ApiResponse;
import com.dianshang.admin.ops.dto.ExpressPreviewVO;
import com.dianshang.admin.ops.service.ExpressService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ops/express")
public class ExpressController {

    private final ExpressService expressService;

    public ExpressController(ExpressService expressService) {
        this.expressService = expressService;
    }

    /**
     * 快递单套打元数据：收发件人、商品清单、运单号等。
     */
    @GetMapping("/preview/{orderId}")
    public ApiResponse<ExpressPreviewVO> preview(@PathVariable String orderId) {
        return ApiResponse.ok(expressService.preview(orderId));
    }
}
