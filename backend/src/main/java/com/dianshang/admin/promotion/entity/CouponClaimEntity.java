package com.dianshang.admin.promotion.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "pms_coupon_claim")
public class CouponClaimEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "coupon_code", nullable = false, length = 20)
    private String couponCode;

    @Column(name = "member_phone", length = 20)
    private String memberPhone;

    @Column(name = "member_no", length = 20)
    private String memberNo;

    @Column(name = "claim_method", length = 30)
    private String claimMethod;

    @Column(name = "claim_time")
    private LocalDateTime claimTime;

    @Column(nullable = false, length = 20)
    private String status = "pending";

    @Column(name = "use_time")
    private LocalDateTime useTime;

    @Column(name = "order_no", length = 20)
    private String orderNo;
}
