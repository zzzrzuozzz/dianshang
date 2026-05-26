package com.dianshang.admin.permission.controller;

import com.dianshang.admin.common.ApiResponse;
import com.dianshang.admin.common.PageResult;
import com.dianshang.admin.permission.dto.InviteCodeGenerateRequest;
import com.dianshang.admin.permission.dto.InviteCodePageRequest;
import com.dianshang.admin.permission.dto.InviteCodeVO;
import com.dianshang.admin.permission.service.InviteCodeService;
import com.dianshang.admin.permission.support.PermissionSecurityHelper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system/invite-code")
public class InviteCodeController {

    private final InviteCodeService inviteCodeService;
    private final PermissionSecurityHelper securityHelper;

    public InviteCodeController(InviteCodeService inviteCodeService,
                                PermissionSecurityHelper securityHelper) {
        this.inviteCodeService = inviteCodeService;
        this.securityHelper = securityHelper;
    }

    @PostMapping("/generate")
    public ApiResponse<List<String>> generate(@Valid @RequestBody InviteCodeGenerateRequest request) {
        securityHelper.assertSuperAdmin();
        List<String> codes = inviteCodeService.generate(request, securityHelper.currentUserId());
        return ApiResponse.ok(codes);
    }

    @PostMapping("/page")
    public ApiResponse<PageResult<InviteCodeVO>> page(@RequestBody InviteCodePageRequest request) {
        securityHelper.assertSuperAdmin();
        return ApiResponse.ok(inviteCodeService.page(request));
    }
}
