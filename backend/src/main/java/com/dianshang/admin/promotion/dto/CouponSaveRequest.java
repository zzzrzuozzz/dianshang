package com.dianshang.admin.promotion.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CouponSaveRequest {

    private String id;
    @NotBlank
    private String name;
    @NotBlank
    private String type;
    @NotBlank
    private String scopeType;
    private List<String> productIds;
    private List<String> categoryIds;
    private BigDecimal thresholdAmount;
    private BigDecimal faceValue;
    private Integer issueQty;
    private String platform;
    private Integer validityDays;
    private String startTime;
    private String endTime;
    private Boolean online;
}
