package com.dianshang.admin.permission.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "sys_role_menu")
@IdClass(SysRoleMenu.RoleMenuId.class)
public class SysRoleMenu {

    @Id
    @Column(name = "role_id")
    private Long roleId;

    @Id
    @Column(name = "menu_id")
    private Long menuId;

    @Getter
    @Setter
    public static class RoleMenuId implements Serializable {
        private Long roleId;
        private Long menuId;
    }
}
