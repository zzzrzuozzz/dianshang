package com.dianshang.admin.order.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AfterSaleListVO {

    private String id;
    private String orderId;
    private String productName;
    private String thumb;
    private String orderStatus;
    private String shipStatus;
    private String afterSaleStatus;
    private String afterSaleType;
    private BigDecimal refundAmount;
    private String applyTime;
    private String processTime;
}
