package com.dianshang.admin.product.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductDetailVO {

    private String id;
    private String category;
    private String name;
    private String subtitle;
    private String brand;
    private String intro;
    private String shipping;
    private List<List<String>> deliveryRegions;
    private String sku;
    private BigDecimal price;
    private BigDecimal marketPrice;
    private Integer stock;
    private Integer stockWarning;
    private String unit;
    private String weight;
    private Boolean preSale;
    private Boolean onSale;
    private List<String> recommend;
    private List<String> services;
    private String tags;
    private List<String> mainImages;
    private String whiteImage;
    private String video;
    private String detail;
    private String status;
    private String auditStatus;
}
