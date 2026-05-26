package com.dianshang.admin.product.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductVO {

    private String id;
    private String title;
    private String subtitle;
    private String thumb;
    private BigDecimal originalPrice;
    private BigDecimal discountPrice;
    private String status;
    private String auditStatus;
    private String remark;
    private String sku;
    private Integer sort;
    private Integer stock;
    private Integer monthSales;
    private Integer totalSales;
    private String supplier;
}
