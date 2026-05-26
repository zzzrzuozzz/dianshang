package com.dianshang.admin.content.help.controller;

import com.dianshang.admin.common.ApiResponse;
import com.dianshang.admin.content.dto.ContentPageVO;
import com.dianshang.admin.content.dto.LongIdsRequest;
import com.dianshang.admin.content.dto.TypeOptionVO;
import com.dianshang.admin.content.help.dto.HelpTypeSaveRequest;
import com.dianshang.admin.content.help.dto.HelpTypeVO;
import com.dianshang.admin.content.help.service.HelpTypeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/content/help/type")
public class HelpTypeController {

    private final HelpTypeService helpTypeService;

    public HelpTypeController(HelpTypeService helpTypeService) {
        this.helpTypeService = helpTypeService;
    }

    @GetMapping("/list")
    public ApiResponse<ContentPageVO<HelpTypeVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "all") String tab,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.ok(helpTypeService.list(keyword, tab, page, pageSize));
    }

    @GetMapping("/options")
    public ApiResponse<List<TypeOptionVO>> options() {
        return ApiResponse.ok(helpTypeService.options());
    }

    @GetMapping("/{id}")
    public ApiResponse<HelpTypeVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(helpTypeService.detail(id));
    }

    @PostMapping("/save")
    public ApiResponse<Long> save(@Valid @RequestBody HelpTypeSaveRequest request) {
        return ApiResponse.ok(helpTypeService.save(request));
    }

    @PutMapping("/{id}/visible")
    public ApiResponse<Void> visible(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        helpTypeService.toggleVisible(id, Boolean.TRUE.equals(body.get("visible")));
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        helpTypeService.delete(id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/batch/delete")
    public ApiResponse<Map<String, Integer>> batchDelete(@Valid @RequestBody LongIdsRequest request) {
        return ApiResponse.ok(Map.of("count", helpTypeService.batchDelete(request)));
    }
}
