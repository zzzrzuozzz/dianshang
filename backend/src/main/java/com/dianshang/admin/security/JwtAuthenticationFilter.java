package com.dianshang.admin.security;

import com.dianshang.admin.permission.service.PermissionQueryService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final PermissionQueryService permissionQueryService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                   PermissionQueryService permissionQueryService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.permissionQueryService = permissionQueryService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = resolveToken(request);
        if (StringUtils.hasText(token)) {
            try {
                Long userId = jwtTokenProvider.getUserId(token);
                permissionQueryService.assertAdminEnabled(userId);
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                permissionQueryService.roleKeysForUser(userId).forEach(key ->
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + key.toUpperCase())));
                permissionQueryService.permsForUser(userId).forEach(perm ->
                        authorities.add(new SimpleGrantedAuthority(perm)));
                var auth = new UsernamePasswordAuthenticationToken(userId, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
