package com.dianshang.admin.auth;

import com.dianshang.admin.auth.dto.LoginRequest;
import com.dianshang.admin.auth.dto.LoginResponse;
import com.dianshang.admin.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    /** 刷新当前登录用户的菜单与权限（角色变更后调用） */
    @GetMapping("/session")
    public ApiResponse<LoginResponse> session(Authentication authentication) {
        Long userId = authentication != null && authentication.getPrincipal() instanceof Long
                ? (Long) authentication.getPrincipal() : null;
        if (userId == null) {
            return ApiResponse.fail(401, "未登录");
        }
        return ApiResponse.ok(authService.refreshSession(userId));
    }
}
