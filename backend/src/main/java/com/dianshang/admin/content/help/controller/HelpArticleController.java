package com.dianshang.admin.content.help.controller;

import com.dianshang.admin.common.ApiResponse;
import com.dianshang.admin.content.dto.ContentPageVO;
import com.dianshang.admin.content.help.dto.HelpArticleListVO;
import com.dianshang.admin.content.help.dto.HelpArticleSaveRequest;
import com.dianshang.admin.content.help.service.HelpArticleService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/content/help")
public class HelpArticleController {

    private final HelpArticleService helpArticleService;

    public HelpArticleController(HelpArticleService helpArticleService) {
        this.helpArticleService = helpArticleService;
    }

    @GetMapping("/list")
    public ApiResponse<ContentPageVO<HelpArticleListVO>> list(
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "all") String tab,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.ok(helpArticleService.list(title, tab, startDate, endDate, page, pageSize));
    }

    @GetMapping("/{articleCode}")
    public ApiResponse<HelpArticleSaveRequest> detail(@PathVariable String articleCode) {
        return ApiResponse.ok(helpArticleService.detail(articleCode));
    }

    @PostMapping("/save")
    public ApiResponse<String> save(@Valid @RequestBody HelpArticleSaveRequest request) {
        return ApiResponse.ok(helpArticleService.save(request));
    }

    @PutMapping("/{articleCode}/status")
    public ApiResponse<Void> status(@PathVariable String articleCode, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        helpArticleService.updateStatus(articleCode, status == null ? 0 : status);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{articleCode}")
    public ApiResponse<Void> delete(@PathVariable String articleCode) {
        helpArticleService.delete(articleCode);
        return ApiResponse.ok(null);
    }
}
