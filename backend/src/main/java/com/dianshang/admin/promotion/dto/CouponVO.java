package com.dianshang.admin.promotion.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CouponVO {

    private String id;
    private String name;
    private String type;
    private String typeLabel;
    private String products;
    private String threshold;
    private BigDecimal faceValue;
    private String issueQty;
    private Integer claimed;
    private Integer used;
    private String platform;
    private String validity;
    private String timeRange;
    private String status;
    private String statusLabel;
    private Boolean online;
    private String scopeType;
}
