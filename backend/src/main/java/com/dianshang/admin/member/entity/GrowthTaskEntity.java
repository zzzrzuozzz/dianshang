package com.dianshang.admin.member.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ums_growth_task")
public class GrowthTaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_code", nullable = false, unique = true, length = 30)
    private String taskCode;

    @Column(name = "task_name", nullable = false, length = 100)
    private String taskName;

    @Column(name = "growth_reward", nullable = false)
    private Integer growthReward = 0;

    @Column(name = "points_reward", nullable = false)
    private Integer pointsReward = 0;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(length = 500)
    private String description;

    @Column(name = "sort_num", nullable = false)
    private Integer sortNum = 0;
}
