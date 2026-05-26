package com.dianshang.admin.permission.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "sys_menu")
public class SysMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parent_id")
    private Long parentId = 0L;

    @Column(name = "menu_name", nullable = false, length = 50)
    private String menuName;

    @Column(name = "menu_type", nullable = false, length = 1)
    private String menuType;

    private String path;

    private String perms;

    private String icon;

    @Column(name = "sort_num")
    private Integer sortNum = 0;

    @Column(name = "create_time")
    private LocalDateTime createTime;
}
