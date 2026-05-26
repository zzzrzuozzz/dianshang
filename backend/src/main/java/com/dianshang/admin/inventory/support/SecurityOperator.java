package com.dianshang.admin.inventory.support;

import com.dianshang.admin.user.AdminUser;
import com.dianshang.admin.user.AdminUserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityOperator {

    private final AdminUserRepository adminUserRepository;

    public SecurityOperator(AdminUserRepository adminUserRepository) {
        this.adminUserRepository = adminUserRepository;
    }

    public String currentUsername() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return "system";
            }
            Object principal = auth.getPrincipal();
            if (principal instanceof Long userId) {
                return adminUserRepository.findById(userId)
                        .map(AdminUser::getUsername)
                        .orElse("admin");
            }
            return String.valueOf(principal);
        } catch (Exception e) {
            return "system";
        }
    }
}
