package com.dianshang.admin.member.controller;

import com.dianshang.admin.common.ApiResponse;
import com.dianshang.admin.member.dto.*;
import com.dianshang.admin.member.service.MemberService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/list")
    public ApiResponse<MemberPageVO> list(
            @RequestParam(required = false) String account,
            @RequestParam(required = false) String nickname,
            @RequestParam(defaultValue = "all") String tab,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.ok(memberService.list(account, nickname, tab, null, startDate, endDate, page, pageSize));
    }

    @GetMapping("/detail/{userNo}")
    public ApiResponse<MemberDetailVO> detail(@PathVariable String userNo) {
        return ApiResponse.ok(memberService.detail(userNo));
    }

    @PostMapping("/update")
    public ApiResponse<Void> update(@Valid @RequestBody MemberUpdateRequest request) {
        memberService.update(request);
        return ApiResponse.ok(null);
    }

    @GetMapping("/export")
    public void export(
            @RequestParam(required = false) String account,
            @RequestParam(required = false) String nickname,
            @RequestParam(defaultValue = "all") String tab,
            HttpServletResponse response) throws IOException {
        writeCsv(response, "用户列表", memberService.exportMembers(account, nickname, tab));
    }

    @GetMapping("/level/options")
    public ApiResponse<List<LevelOptionVO>> levelOptions() {
        return ApiResponse.ok(memberService.levelOptions());
    }

    @GetMapping("/level/list")
    public ApiResponse<List<MemberLevelVO>> levelList(@RequestParam(required = false) String name) {
        return ApiResponse.ok(memberService.levelList(name));
    }

    @GetMapping("/level/detail/{levelCode}")
    public ApiResponse<MemberLevelVO> levelDetail(@PathVariable String levelCode) {
        return ApiResponse.ok(memberService.levelDetail(levelCode));
    }

    @PostMapping("/level/save")
    public ApiResponse<String> saveLevel(@Valid @RequestBody MemberLevelSaveRequest request) {
        return ApiResponse.ok(memberService.saveLevel(request));
    }

    @DeleteMapping("/level/{levelCode}")
    public ApiResponse<Void> deleteLevel(@PathVariable String levelCode) {
        memberService.deleteLevel(levelCode);
        return ApiResponse.ok(null);
    }

    @GetMapping("/tag/list")
    public ApiResponse<List<MemberTagVO>> tagList(@RequestParam(required = false) String keyword) {
        return ApiResponse.ok(memberService.tagList(keyword));
    }

    @GetMapping("/tag/detail/{tagCode}")
    public ApiResponse<MemberTagSaveRequest> tagDetail(@PathVariable String tagCode) {
        return ApiResponse.ok(memberService.tagDetail(tagCode));
    }

    @PostMapping("/tag/save")
    public ApiResponse<String> saveTag(@Valid @RequestBody MemberTagSaveRequest request) {
        return ApiResponse.ok(memberService.saveTag(request));
    }

    @DeleteMapping("/tag/{tagCode}")
    public ApiResponse<Void> deleteTag(@PathVariable String tagCode) {
        memberService.deleteTag(tagCode);
        return ApiResponse.ok(null);
    }

    @GetMapping("/tag/users")
    public ApiResponse<MemberPageVO> tagUsers(
            @RequestParam String tagId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.ok(memberService.tagUsers(tagId, page, pageSize));
    }

    @PostMapping("/tag/users/remove")
    public ApiResponse<Void> removeTagUser(@RequestParam String tagId, @RequestParam String userNo) {
        memberService.removeTagUser(tagId, userNo);
        return ApiResponse.ok(null);
    }

    @GetMapping("/growth/list")
    public ApiResponse<MemberPageVO> growthList(
            @RequestParam(required = false) String account,
            @RequestParam(required = false) String nickname,
            @RequestParam(defaultValue = "all") String tab,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.ok(memberService.growthList(account, nickname, tab, page, pageSize));
    }

    @PostMapping("/growth/adjust")
    public ApiResponse<Void> adjust(@Valid @RequestBody GrowthAdjustRequest request) {
        memberService.adjust(request);
        return ApiResponse.ok(null);
    }

    @PostMapping("/growth/batch")
    public ApiResponse<Void> batch(@Valid @RequestBody GrowthBatchRequest request) {
        memberService.batchAdjust(request);
        return ApiResponse.ok(null);
    }

    @GetMapping("/growth/tasks")
    public ApiResponse<List<GrowthTaskVO>> tasks() {
        return ApiResponse.ok(memberService.listTasks());
    }

    @PostMapping("/growth/tasks/save")
    public ApiResponse<Void> saveTasks(@RequestBody List<GrowthTaskVO> tasks) {
        memberService.saveTasks(tasks);
        return ApiResponse.ok(null);
    }

    @GetMapping("/growth/rules")
    public ApiResponse<GrowthRulesVO> rules() {
        return ApiResponse.ok(memberService.getRules());
    }

    @PostMapping("/growth/rules/save")
    public ApiResponse<Void> saveRules(@RequestBody GrowthRulesVO rules) {
        memberService.saveRules(rules);
        return ApiResponse.ok(null);
    }

    @GetMapping("/growth/rewards")
    public ApiResponse<GrowthRewardsVO> rewards() {
        return ApiResponse.ok(memberService.getRewards());
    }

    @PostMapping("/growth/rewards/save")
    public ApiResponse<Void> saveRewards(@RequestBody GrowthRewardsVO rewards) {
        memberService.saveRewards(rewards);
        return ApiResponse.ok(null);
    }

    @GetMapping("/growth/ledger")
    public ApiResponse<LedgerPageVO> ledger(
            @RequestParam(defaultValue = "growth") String ledgerType,
            @RequestParam(required = false) String account,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.ok(memberService.ledgerList(ledgerType, account, page, pageSize));
    }

    @GetMapping("/growth/export")
    public void growthExport(
            @RequestParam(required = false) String account,
            @RequestParam(defaultValue = "all") String tab,
            HttpServletResponse response) throws IOException {
        writeCsv(response, "成长积分", memberService.exportGrowth(account, tab));
    }

    private void writeCsv(HttpServletResponse response, String prefix, byte[] csv) throws IOException {
        String filename = URLEncoder.encode(prefix + "_" + System.currentTimeMillis() + ".csv",
                StandardCharsets.UTF_8).replace("+", "%20");
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename);
        response.getOutputStream().write(csv);
        response.flushBuffer();
    }
}
