package com.dianshang.admin.permission.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "sys_invite_code")
public class SysInviteCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String code;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Column(nullable = false)
    private Integer used = 0;

    @Column(name = "used_by_admin_id")
    private Long usedByAdminId;

    @Column(name = "used_account", length = 100)
    private String usedAccount;

    @Column(name = "used_account_type", length = 10)
    private String usedAccountType;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(length = 255)
    private String remark;

    @Column(name = "used_time")
    private LocalDateTime usedTime;

    @Column(name = "create_time")
    private LocalDateTime createTime;
}
