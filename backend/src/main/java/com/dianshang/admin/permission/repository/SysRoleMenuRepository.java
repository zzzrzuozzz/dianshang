package com.dianshang.admin.permission.repository;

import com.dianshang.admin.permission.entity.SysRoleMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SysRoleMenuRepository extends JpaRepository<SysRoleMenu, SysRoleMenu.RoleMenuId> {

    List<SysRoleMenu> findByRoleId(Long roleId);

    List<SysRoleMenu> findByRoleIdIn(List<Long> roleIds);

    @Modifying
    @Query("delete from SysRoleMenu rm where rm.roleId = :roleId")
    void deleteByRoleId(Long roleId);

    boolean existsByMenuId(Long menuId);
}
