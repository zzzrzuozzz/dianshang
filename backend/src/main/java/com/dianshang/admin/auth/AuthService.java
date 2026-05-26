package com.dianshang.admin.auth;

import com.dianshang.admin.auth.dto.LoginRequest;
import com.dianshang.admin.auth.dto.LoginResponse;
import com.dianshang.admin.common.BusinessException;
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

    public AuthService(AdminUserRepository adminUserRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public LoginResponse login(LoginRequest request) {
        AdminUser user = adminUserRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(401, "账号或密码错误"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "账号或密码错误");
        }
        String token = jwtTokenProvider.createToken(user.getId(), user.getUsername());
        return new LoginResponse(
                token,
                "Bearer",
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getAvatar()
        );
    }
}
