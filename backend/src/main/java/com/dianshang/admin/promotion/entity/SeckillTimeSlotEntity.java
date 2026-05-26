package com.dianshang.admin.promotion.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "pms_seckill_time_slot")
public class SeckillTimeSlotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "slot_code", nullable = false, unique = true, length = 20)
    private String slotCode;

    @Column(name = "slot_name", nullable = false, length = 100)
    private String slotName;

    @Column(name = "start_time", nullable = false, length = 12)
    private String startTime;

    @Column(name = "end_time", nullable = false, length = 12)
    private String endTime;

    @Column(nullable = false)
    private Boolean enabled = true;
}
