package com.dianshang.admin.promotion.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class GroupBuySkuVO {

    private String id;
    private String name;
    private String productCode;
    private BigDecimal price;
    private BigDecimal groupPrice;
    private Integer groupSize;
    private Integer groupQty;
    private Integer remainStock;
    private Integer totalStock;
    private Integer warningStock;
    private Integer limitQty;
    private Integer sort;
    private Map<String, Object> attrs;
}
