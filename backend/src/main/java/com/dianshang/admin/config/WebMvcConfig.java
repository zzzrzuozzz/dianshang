package com.dianshang.admin.config;

import com.dianshang.admin.permission.support.PermissionAdminInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final UploadProperties uploadProperties;
    private final PermissionAdminInterceptor permissionAdminInterceptor;

    public WebMvcConfig(UploadProperties uploadProperties,
                        PermissionAdminInterceptor permissionAdminInterceptor) {
        this.uploadProperties = uploadProperties;
        this.permissionAdminInterceptor = permissionAdminInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(permissionAdminInterceptor)
                .addPathPatterns(
                        "/api/system/menu/**",
                        "/api/system/role/**",
                        "/api/system/admin/**",
                        "/api/system/invite-code/**"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path root = Path.of(uploadProperties.getDir()).toAbsolutePath().normalize();
        String location = "file:" + root.toString().replace('\\', '/') + "/";
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }
}
