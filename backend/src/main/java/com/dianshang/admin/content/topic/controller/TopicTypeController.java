package com.dianshang.admin.content.topic.controller;

import com.dianshang.admin.common.ApiResponse;
import com.dianshang.admin.content.dto.ContentPageVO;
import com.dianshang.admin.content.dto.LongIdsRequest;
import com.dianshang.admin.content.dto.TypeOptionVO;
import com.dianshang.admin.content.topic.dto.TopicTypeSaveRequest;
import com.dianshang.admin.content.topic.dto.TopicTypeVO;
import com.dianshang.admin.content.topic.service.TopicTypeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/content/topic/type")
public class TopicTypeController {

    private final TopicTypeService topicTypeService;

    public TopicTypeController(TopicTypeService topicTypeService) {
        this.topicTypeService = topicTypeService;
    }

    @GetMapping("/list")
    public ApiResponse<ContentPageVO<TopicTypeVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "all") String tab,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.ok(topicTypeService.list(keyword, tab, page, pageSize));
    }

    @GetMapping("/options")
    public ApiResponse<List<TypeOptionVO>> options() {
        return ApiResponse.ok(topicTypeService.options());
    }

    @GetMapping("/{id}")
    public ApiResponse<TopicTypeVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(topicTypeService.detail(id));
    }

    @PostMapping("/save")
    public ApiResponse<Long> save(@Valid @RequestBody TopicTypeSaveRequest request) {
        return ApiResponse.ok(topicTypeService.save(request));
    }

    @PutMapping("/{id}/visible")
    public ApiResponse<Void> visible(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        topicTypeService.toggleVisible(id, Boolean.TRUE.equals(body.get("visible")));
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        topicTypeService.delete(id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/batch/delete")
    public ApiResponse<Map<String, Integer>> batchDelete(@Valid @RequestBody LongIdsRequest request) {
        return ApiResponse.ok(Map.of("count", topicTypeService.batchDelete(request)));
    }
}
