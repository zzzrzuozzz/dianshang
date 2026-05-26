package com.dianshang.admin.permission.support;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * RBAC 管理接口仅超级管理员可访问（菜单/角色/管理员维护）。
 */
@Component
public class PermissionAdminInterceptor implements HandlerInterceptor {

    private final PermissionSecurityHelper securityHelper;

    public PermissionAdminInterceptor(PermissionSecurityHelper securityHelper) {
        this.securityHelper = securityHelper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        securityHelper.assertSuperAdmin();
        return true;
    }
}
