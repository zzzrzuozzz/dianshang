package com.dianshang.admin.auth;

import com.dianshang.admin.auth.dto.ForgotPasswordRequest;
import com.dianshang.admin.auth.dto.LoginRequest;
import com.dianshang.admin.auth.dto.LoginResponse;
import com.dianshang.admin.auth.dto.RegisterRequest;
import com.dianshang.admin.common.BusinessException;
import com.dianshang.admin.permission.entity.SysInviteCode;
import com.dianshang.admin.permission.entity.SysRole;
import com.dianshang.admin.permission.repository.SysRoleRepository;
import com.dianshang.admin.permission.service.InviteCodeService;
import com.dianshang.admin.permission.service.PermissionQueryService;
import com.dianshang.admin.security.JwtTokenProvider;
import com.dianshang.admin.user.AdminUser;
import com.dianshang.admin.user.AdminUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Service
public class AuthService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final PermissionQueryService permissionQueryService;
    private final InviteCodeService inviteCodeService;
    private final SysRoleRepository roleRepository;

    public AuthService(AdminUserRepository adminUserRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       PermissionQueryService permissionQueryService,
                       InviteCodeService inviteCodeService,
                       SysRoleRepository roleRepository) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.permissionQueryService = permissionQueryService;
        this.inviteCodeService = inviteCodeService;
        this.roleRepository = roleRepository;
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
        return buildLoginResponse(user);
    }

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        String accountType = request.getAccountType().toUpperCase();
        String account = request.getAccount().trim();
        String username = request.getUsername().trim();

        validateAccount(accountType, account);
        if (adminUserRepository.existsByUsername(username)) {
            throw new BusinessException("用户名已存在，请更换");
        }
        if ("PHONE".equals(accountType)) {
            if (adminUserRepository.existsByPhone(account)) {
                throw new BusinessException("该手机号已注册");
            }
        } else if (adminUserRepository.existsByEmail(account)) {
            throw new BusinessException("该邮箱已注册");
        }

        AdminUser user = new AdminUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(StringUtils.hasText(request.getNickname()) ? request.getNickname().trim() : username);
        if ("PHONE".equals(accountType)) {
            user.setPhone(account);
        } else {
            user.setEmail(account);
        }
        user.setStatus(1);
        user.setGender("unknown");
        user.setAvatar("https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png");
        user.setCreateTime(LocalDateTime.now());
        adminUserRepository.save(user);

        SysInviteCode invite = inviteCodeService.consumeForRegister(
                request.getInviteCode(), accountType, account, user.getId());
        SysRole role = roleRepository.findById(invite.getRoleId()).orElse(null);
        user.setRoleName(role != null ? role.getRoleName() : "未分配");
        adminUserRepository.save(user);

        return buildLoginResponse(user);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        String accountType = request.getAccountType().toUpperCase();
        String account = request.getAccount().trim();
        validateAccount(accountType, account);

        Long adminId = inviteCodeService.adminIdForReset(request.getInviteCode(), accountType, account);
        AdminUser user = adminUserRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException("账号不存在"));
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已禁用，请联系管理员");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        adminUserRepository.save(user);
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

    private LoginResponse buildLoginResponse(AdminUser user) {
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

    private void validateAccount(String accountType, String account) {
        if ("PHONE".equals(accountType)) {
            if (!PHONE_PATTERN.matcher(account).matches()) {
                throw new BusinessException("手机号格式不正确");
            }
        } else if ("EMAIL".equals(accountType)) {
            if (!EMAIL_PATTERN.matcher(account).matches()) {
                throw new BusinessException("邮箱格式不正确");
            }
        } else {
            throw new BusinessException("注册方式无效");
        }
    }
}
