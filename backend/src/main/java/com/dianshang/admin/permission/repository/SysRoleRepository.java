package com.dianshang.admin.permission.repository;

import com.dianshang.admin.permission.entity.SysRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface SysRoleRepository extends JpaRepository<SysRole, Long>, JpaSpecificationExecutor<SysRole> {

    Optional<SysRole> findByRoleKey(String roleKey);

    List<SysRole> findByStatusOrderBySortNumAsc(Integer status);

    List<SysRole> findAllByOrderBySortNumAsc();
}
