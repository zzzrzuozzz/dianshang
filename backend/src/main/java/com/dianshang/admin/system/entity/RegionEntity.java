package com.dianshang.admin.system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "sys_region", indexes = @Index(name = "idx_parent_code", columnList = "parent_code"))
public class RegionEntity {

    @Id
    @Column(length = 20)
    private String code;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "parent_code", length = 20)
    private String parentCode = "0";

    @Column(name = "level_num", nullable = false)
    private Integer levelNum;
}
