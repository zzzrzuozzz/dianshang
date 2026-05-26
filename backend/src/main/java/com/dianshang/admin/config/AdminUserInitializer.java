package com.dianshang.admin.config;

import com.dianshang.admin.user.AdminUser;
import com.dianshang.admin.user.AdminUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AdminUserInitializer implements CommandLineRunner {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserInitializer(AdminUserRepository adminUserRepository, PasswordEncoder passwordEncoder) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (adminUserRepository.findByUsername("admin").isEmpty()) {
            AdminUser user = new AdminUser();
            user.setUsername("admin");
            user.setPassword(passwordEncoder.encode("admin123"));
            user.setNickname("暴走管理员");
            user.setPhone("13066660000");
            user.setEmail("admin@baozou.com");
            user.setGender("male");
            user.setAvatar("https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png");
            user.setRoleName("超级管理员");
            user.setCreateTime(LocalDateTime.now());
            adminUserRepository.save(user);
        }
    }
}
