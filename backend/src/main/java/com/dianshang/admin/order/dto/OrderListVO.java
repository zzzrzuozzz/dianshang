package com.dianshang.admin.order.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderListVO {

    private String id;
    private String productName;
    private String thumb;
    private String spec;
    private Integer quantity;
    private BigDecimal actualAmount;
    private BigDecimal discount;
    private BigDecimal freight;
    private Boolean freightFree;
    private String orderStatus;
    private String shipStatus;
    private String logistics;
    private String payTime;
    private String autoConfirmTime;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String supplier;
    private String supplierPhone;
}
