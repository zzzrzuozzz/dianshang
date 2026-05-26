package com.dianshang.admin.promotion.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "pms_coupon")
public class CouponEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "coupon_code", nullable = false, unique = true, length = 20)
    private String couponCode;

    @Column(name = "coupon_name", nullable = false, length = 200)
    private String couponName;

    @Column(name = "coupon_type", nullable = false, length = 30)
    private String couponType;

    @Column(name = "scope_type", nullable = false, length = 20)
    private String scopeType = "all";

    @Column(name = "scope_json", columnDefinition = "CLOB")
    private String scopeJson;

    @Column(name = "threshold_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal thresholdAmount = BigDecimal.ZERO;

    @Column(name = "face_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal faceValue = BigDecimal.ZERO;

    @Column(name = "issue_qty", nullable = false)
    private Integer issueQty = -1;

    @Column(name = "claimed_qty", nullable = false)
    private Integer claimedQty = 0;

    @Column(name = "used_qty", nullable = false)
    private Integer usedQty = 0;

    @Column(length = 30)
    private String platform;

    @Column(name = "validity_days", nullable = false)
    private Integer validityDays = 15;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(nullable = false)
    private Boolean online = true;

    @Column(nullable = false)
    private Boolean deleted = false;
}
