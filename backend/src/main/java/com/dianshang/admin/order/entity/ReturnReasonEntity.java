package com.dianshang.admin.order.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "oms_return_reason")
public class ReturnReasonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reason_code", nullable = false, unique = true, length = 20)
    private String reasonCode;

    @Column(name = "reason_text", nullable = false, length = 200)
    private String reasonText;

    @Column(name = "add_time", nullable = false)
    private LocalDateTime addTime = LocalDateTime.now();

    @Column(nullable = false)
    private Boolean visible = true;

    @Column(name = "sort_num", nullable = false)
    private Integer sortNum = 0;

    @Column(nullable = false)
    private Boolean deleted = false;
}
