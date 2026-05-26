package com.dianshang.admin.permission.repository;

import com.dianshang.admin.permission.entity.SysInviteCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface SysInviteCodeRepository extends JpaRepository<SysInviteCode, Long>, JpaSpecificationExecutor<SysInviteCode> {

    Optional<SysInviteCode> findByCode(String code);

    boolean existsByCode(String code);
}
