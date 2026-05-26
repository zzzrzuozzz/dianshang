package com.dianshang.admin.product.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CommentListVO {

    private String id;
    private String title;
    private String thumb;
    private String rating;
    private String content;
    private BigDecimal originalPrice;
    private BigDecimal discountPrice;
    private String status;
    private String sku;
    private Integer sort;
    private Integer stock;
    private Integer sales;
    private Integer totalGood;
    private Integer totalNeutral;
    private Integer totalBad;
}
