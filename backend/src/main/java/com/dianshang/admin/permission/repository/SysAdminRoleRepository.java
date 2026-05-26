package com.dianshang.admin.permission.repository;

import com.dianshang.admin.permission.entity.SysAdminRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SysAdminRoleRepository extends JpaRepository<SysAdminRole, SysAdminRole.AdminRoleId> {

    List<SysAdminRole> findByAdminId(Long adminId);

    @Modifying
    @Query("delete from SysAdminRole ar where ar.adminId = :adminId")
    void deleteByAdminId(Long adminId);

    long countByRoleId(Long roleId);
}
