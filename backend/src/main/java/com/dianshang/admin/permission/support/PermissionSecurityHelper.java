package com.dianshang.admin.permission.support;

import com.dianshang.admin.common.BusinessException;
import com.dianshang.admin.permission.service.PermissionQueryService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class PermissionSecurityHelper {

    private final PermissionQueryService permissionQueryService;

    public PermissionSecurityHelper(PermissionQueryService permissionQueryService) {
        this.permissionQueryService = permissionQueryService;
    }

    public Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long userId) {
            return userId;
        }
        return null;
    }

    public void assertSuperAdmin() {
        Long userId = currentUserId();
        if (userId == null || !permissionQueryService.isSuperAdmin(userId)) {
            throw new BusinessException(403, "需要超级管理员权限");
        }
    }

    public void assertPerm(String perm) {
        Long userId = currentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        permissionQueryService.assertPerm(userId, perm);
    }
}
