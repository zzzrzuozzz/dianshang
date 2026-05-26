package com.dianshang.admin.permission.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "sys_role")
public class SysRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_name", nullable = false, length = 50)
    private String roleName;

    @Column(name = "role_key", nullable = false, unique = true, length = 50)
    private String roleKey;

    @Column(name = "sort_num")
    private Integer sortNum = 0;

    private Integer status = 1;

    private String remark;

    @Column(name = "create_time")
    private LocalDateTime createTime;
}
