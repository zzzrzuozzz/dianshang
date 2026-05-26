package com.dianshang.admin.inventory.controller;

import com.dianshang.admin.common.ApiResponse;
import com.dianshang.admin.inventory.dto.InventoryPageVO;
import com.dianshang.admin.inventory.dto.InventoryUpdateRequest;
import com.dianshang.admin.inventory.dto.StockFlowPageVO;
import com.dianshang.admin.inventory.service.InventoryService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/list")
    public ApiResponse<InventoryPageVO> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String supplier,
            @RequestParam(required = false) String stockStatus,
            @RequestParam(defaultValue = "all") String tab,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.ok(inventoryService.list(keyword, category, supplier, stockStatus, tab, page, pageSize));
    }

    @PostMapping("/update")
    public ApiResponse<Void> update(@Valid @RequestBody InventoryUpdateRequest request) {
        inventoryService.updateStock(request);
        return ApiResponse.ok(null);
    }

    @GetMapping("/flow")
    public ApiResponse<StockFlowPageVO> flow(
            @RequestParam(required = false) String flowNo,
            @RequestParam(required = false) String product,
            @RequestParam(required = false) String bizType,
            @RequestParam(defaultValue = "all") String tab,
            @RequestParam(required = false) String goodsId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.ok(inventoryService.flowList(flowNo, product, bizType, tab, goodsId,
                startDate, endDate, page, pageSize));
    }

    @GetMapping("/flow/export")
    public void exportFlow(
            @RequestParam(required = false) String flowNo,
            @RequestParam(required = false) String product,
            @RequestParam(required = false) String bizType,
            @RequestParam(defaultValue = "all") String tab,
            @RequestParam(required = false) String goodsId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletResponse response) throws IOException {
        byte[] csv = inventoryService.exportFlowCsv(flowNo, product, bizType, tab, goodsId, startDate, endDate);
        String filename = URLEncoder.encode("库存流水_" + System.currentTimeMillis() + ".csv", StandardCharsets.UTF_8)
                .replace("+", "%20");
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename);
        response.getOutputStream().write(csv);
        response.flushBuffer();
    }
}
