package com.dianshang.admin.permission.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "sys_admin_role")
@IdClass(SysAdminRole.AdminRoleId.class)
public class SysAdminRole {

    @Id
    @Column(name = "admin_id")
    private Long adminId;

    @Id
    @Column(name = "role_id")
    private Long roleId;

    @Getter
    @Setter
    public static class AdminRoleId implements Serializable {
        private Long adminId;
        private Long roleId;
    }
}
