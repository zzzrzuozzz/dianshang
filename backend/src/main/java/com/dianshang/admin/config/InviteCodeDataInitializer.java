package com.dianshang.admin.config;

import com.dianshang.admin.permission.entity.SysInviteCode;
import com.dianshang.admin.permission.entity.SysRole;
import com.dianshang.admin.permission.repository.SysInviteCodeRepository;
import com.dianshang.admin.permission.repository.SysRoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@Order(26)
public class InviteCodeDataInitializer implements CommandLineRunner {

    private final SysInviteCodeRepository inviteCodeRepository;
    private final SysRoleRepository roleRepository;

    public InviteCodeDataInitializer(SysInviteCodeRepository inviteCodeRepository,
                                       SysRoleRepository roleRepository) {
        this.inviteCodeRepository = inviteCodeRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (inviteCodeRepository.count() > 0) {
            return;
        }
        roleRepository.findByRoleKey("operator").ifPresent(role -> {
            SysInviteCode code = new SysInviteCode();
            code.setCode("DEMO2026REG01");
            code.setRoleId(role.getId());
            code.setUsed(0);
            code.setRemark("演示邀请码，注册后可在角色管理中调整权限");
            code.setCreateTime(LocalDateTime.now());
            inviteCodeRepository.save(code);
        });
    }
}
