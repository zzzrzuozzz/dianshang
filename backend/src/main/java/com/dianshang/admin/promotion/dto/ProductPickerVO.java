package com.dianshang.admin.promotion.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductPickerVO {

    private String id;
    private String name;
    private BigDecimal price;
    private Integer stock;
}
