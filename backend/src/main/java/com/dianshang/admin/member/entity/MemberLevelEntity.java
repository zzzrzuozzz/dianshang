package com.dianshang.admin.member.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "ums_member_level")
public class MemberLevelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "level_code", nullable = false, unique = true, length = 30)
    private String levelCode;

    @Column(name = "level_name", nullable = false, length = 100)
    private String levelName;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @Column(name = "growth_point", nullable = false)
    private Integer growthPoint = 0;

    @Column(name = "free_ship_amount", precision = 10, scale = 2)
    private BigDecimal freeShipAmount;

    @Column(name = "free_ship_times")
    private Integer freeShipTimes;

    @Column(name = "review_growth")
    private Integer reviewGrowth;

    @Column(name = "review_times")
    private Integer reviewTimes;

    @Column(name = "privileges_json", columnDefinition = "CLOB")
    private String privilegesJson;

    @Column(name = "sort_num", nullable = false)
    private Integer sortNum = 0;

    @Column(nullable = false)
    private Boolean deleted = false;
}
