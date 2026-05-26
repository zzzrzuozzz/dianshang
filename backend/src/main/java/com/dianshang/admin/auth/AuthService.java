package com.dianshang.admin.auth;

import com.dianshang.admin.auth.dto.LoginRequest;
import com.dianshang.admin.auth.dto.LoginResponse;
import com.dianshang.admin.common.BusinessException;
import com.dianshang.admin.permission.service.PermissionQueryService;
import com.dianshang.admin.security.JwtTokenProvider;
import com.dianshang.admin.user.AdminUser;
import com.dianshang.admin.user.AdminUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final PermissionQueryService permissionQueryService;

    public AuthService(AdminUserRepository adminUserRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       PermissionQueryService permissionQueryService) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.permissionQueryService = permissionQueryService;
    }

    public LoginResponse login(LoginRequest request) {
        AdminUser user = adminUserRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(401, "账号或密码错误"));
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(403, "账号已禁用，请联系管理员");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "账号或密码错误");
        }
        String token = jwtTokenProvider.createToken(user.getId(), user.getUsername());
        LoginResponse resp = new LoginResponse(
                token,
                "Bearer",
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getAvatar()
        );
        resp.setMenus(permissionQueryService.menusForUser(user.getId()));
        resp.setRoleKeys(permissionQueryService.roleKeysForUser(user.getId()));
        resp.setPerms(permissionQueryService.permsForUser(user.getId()));
        return resp;
    }

    public LoginResponse refreshSession(Long userId) {
        AdminUser user = adminUserRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(401, "用户不存在"));
        permissionQueryService.assertAdminEnabled(userId);
        LoginResponse resp = new LoginResponse(null, "Bearer", user.getId(),
                user.getUsername(), user.getNickname(), user.getAvatar());
        resp.setMenus(permissionQueryService.menusForUser(userId));
        resp.setRoleKeys(permissionQueryService.roleKeysForUser(userId));
        resp.setPerms(permissionQueryService.permsForUser(userId));
        return resp;
    }
}
