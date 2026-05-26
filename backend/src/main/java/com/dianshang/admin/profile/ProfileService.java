package com.dianshang.admin.profile;

import com.dianshang.admin.common.BusinessException;
import com.dianshang.admin.profile.dto.ProfileUpdateRequest;
import com.dianshang.admin.profile.dto.ProfileVO;
import com.dianshang.admin.profile.dto.UpdatePasswordRequest;
import com.dianshang.admin.user.AdminUser;
import com.dianshang.admin.user.AdminUserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class ProfileService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileService(AdminUserRepository adminUserRepository, PasswordEncoder passwordEncoder) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public ProfileVO getCurrentProfile() {
        AdminUser user = currentUser();
        return toVO(user);
    }

    public ProfileVO updateProfile(ProfileUpdateRequest request) {
        AdminUser user = currentUser();
        user.setNickname(request.getNickname());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setGender(request.getGender());
        adminUserRepository.save(user);
        return toVO(user);
    }

    public void updatePassword(UpdatePasswordRequest request) {
        AdminUser user = currentUser();
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码不正确");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        adminUserRepository.save(user);
    }

    private AdminUser currentUser() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return adminUserRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(401, "登录已失效，请重新登录"));
    }

    private ProfileVO toVO(AdminUser user) {
        ProfileVO vo = new ProfileVO();
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setGender(user.getGender());
        vo.setAvatar(user.getAvatar());
        vo.setRoleName(user.getRoleName());
        if (user.getCreateTime() != null) {
            vo.setCreateTime(user.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        }
        return vo;
    }
}
