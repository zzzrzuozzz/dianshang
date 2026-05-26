package com.dianshang.admin.promotion.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "pms_group_buy_activity")
public class GroupBuyActivityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "activity_code", nullable = false, unique = true, length = 20)
    private String activityCode;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false)
    private Boolean online = true;

    @Column(name = "warning_msg", length = 500)
    private String warningMsg;

    @Column(nullable = false)
    private Boolean deleted = false;
}
