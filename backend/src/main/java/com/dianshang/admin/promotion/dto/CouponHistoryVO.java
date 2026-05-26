package com.dianshang.admin.promotion.dto;

import lombok.Data;

@Data
public class CouponHistoryVO {

    private String couponId;
    private String member;
    private String method;
    private String claimTime;
    private String status;
    private String useTime;
    private String orderId;
}
