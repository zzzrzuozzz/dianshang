package com.dianshang.admin.content.topic.controller;

import com.dianshang.admin.common.ApiResponse;
import com.dianshang.admin.content.dto.ContentPageVO;
import com.dianshang.admin.content.dto.IdsRequest;
import com.dianshang.admin.content.topic.dto.*;
import com.dianshang.admin.content.topic.service.TopicService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/content/topic")
public class TopicController {

    private final TopicService topicService;

    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    @GetMapping("/list")
    public ApiResponse<ContentPageVO<TopicListVO>> list(
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "all") String tab,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.ok(topicService.list(title, tab, startDate, endDate, page, pageSize));
    }

    @GetMapping("/detail/{topicCode}")
    public ApiResponse<TopicDetailVO> detail(@PathVariable String topicCode) {
        return ApiResponse.ok(topicService.detail(topicCode));
    }

    @GetMapping("/{topicCode}")
    public ApiResponse<TopicDetailVO> editForm(@PathVariable String topicCode) {
        return ApiResponse.ok(topicService.editForm(topicCode));
    }

    @PostMapping("/save")
    public ApiResponse<String> save(@Valid @RequestBody TopicSaveRequest request) {
        return ApiResponse.ok(topicService.save(request));
    }

    @PutMapping("/{topicCode}/status")
    public ApiResponse<Void> status(@PathVariable String topicCode, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        topicService.updateStatus(topicCode, status == null ? 0 : status);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{topicCode}")
    public ApiResponse<Void> delete(@PathVariable String topicCode) {
        topicService.delete(topicCode);
        return ApiResponse.ok(null);
    }

    @PostMapping("/comment/reply")
    public ApiResponse<Void> reply(@Valid @RequestBody TopicCommentReplyRequest request) {
        topicService.replyComment(request);
        return ApiResponse.ok(null);
    }

    @PostMapping("/comment/review")
    public ApiResponse<Void> review(@Valid @RequestBody TopicCommentReviewRequest request) {
        topicService.reviewComment(request);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/comment/{commentCode}")
    public ApiResponse<Void> deleteComment(@PathVariable String commentCode) {
        topicService.deleteComment(commentCode);
        return ApiResponse.ok(null);
    }

    @PostMapping("/comment/batch/delete")
    public ApiResponse<Map<String, Integer>> batchDeleteComments(@Valid @RequestBody IdsRequest request) {
        return ApiResponse.ok(Map.of("count", topicService.batchDeleteComments(request)));
    }
}
