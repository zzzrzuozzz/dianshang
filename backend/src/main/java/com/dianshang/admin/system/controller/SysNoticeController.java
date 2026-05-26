package com.dianshang.admin.system.controller;

import com.dianshang.admin.common.ApiResponse;
import com.dianshang.admin.common.PageResult;
import com.dianshang.admin.system.dto.NoticeIdsRequest;
import com.dianshang.admin.system.dto.NoticePageRequest;
import com.dianshang.admin.system.dto.NoticeUnreadSummaryVO;
import com.dianshang.admin.system.dto.NoticeVO;
import com.dianshang.admin.system.service.SysNoticeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system/notice")
public class SysNoticeController {

    private final SysNoticeService noticeService;

    public SysNoticeController(SysNoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @GetMapping("/unread-list")
    public ApiResponse<List<NoticeVO>> unreadList(
            @RequestParam(defaultValue = "5") int limit,
            @RequestParam(defaultValue = "false") boolean urgent) {
        return ApiResponse.ok(noticeService.getTopUnread(limit, urgent));
    }

    @GetMapping("/unread-summary")
    public ApiResponse<NoticeUnreadSummaryVO> unreadSummary() {
        return ApiResponse.ok(noticeService.unreadSummary());
    }

    @PostMapping("/page")
    public ApiResponse<PageResult<NoticeVO>> page(@RequestBody NoticePageRequest query) {
        return ApiResponse.ok(noticeService.queryPage(query));
    }

    @PutMapping("/read")
    public ApiResponse<Void> markRead(@Valid @RequestBody NoticeIdsRequest body) {
        noticeService.markAsRead(body.getIds());
        return ApiResponse.ok(null);
    }

    @PutMapping("/read-all")
    public ApiResponse<Void> markAllRead(@RequestBody(required = false) Map<String, String> body) {
        String noticeType = body != null ? body.get("noticeType") : null;
        noticeService.markAllRead(noticeType);
        return ApiResponse.ok(null);
    }

    @PostMapping("/delete")
    public ApiResponse<Void> delete(@Valid @RequestBody NoticeIdsRequest body) {
        noticeService.deleteByIds(body.getIds());
        return ApiResponse.ok(null);
    }

    @GetMapping("/{id}")
    public ApiResponse<NoticeVO> detail(@PathVariable Long id) {
        NoticeVO vo = noticeService.getDetail(id);
        return ApiResponse.ok(vo);
    }
}
