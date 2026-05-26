package com.dianshang.admin.promotion.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SeckillSkuVO {

    private String id;
    private String name;
    private String productCode;
    private BigDecimal price;
    private BigDecimal seckillPrice;
    private Integer seckillQty;
    private Integer remainStock;
    private Integer totalStock;
    private Integer warningStock;
    private Integer limitQty;
    private Integer sort;
}
