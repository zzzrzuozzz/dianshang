package com.dianshang.admin.promotion.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CouponDetailVO {

    private String id;
    private String name;
    private String type;
    private String products;
    private String threshold;
    private BigDecimal faceValue;
    private BigDecimal thresholdAmount;
    private String couponType;
    private String status;
    private String timeRange;
    private String validity;
    private Integer totalIssue;
    private Integer remain;
    private Integer claimed;
    private Integer used;
    private Integer pending;
    private Integer expired;
    private String scopeType;
    private List<String> productIds;
    private List<String> categoryIds;
    private Boolean online;
    private String startTime;
    private String endTime;
    private Integer issueQty;
    private String platform;
}
