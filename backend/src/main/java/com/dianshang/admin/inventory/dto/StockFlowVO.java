package com.dianshang.admin.inventory.dto;

import lombok.Data;

@Data
public class StockFlowVO {

    private String id;
    private String goodsId;
    private String relatedNo;
    private String orderId;
    private String thumb;
    private String name;
    private String skuName;
    private String skuId;
    private String type;
    private Integer beforeQty;
    private Integer changeQty;
    private Integer afterQty;
    private String operator;
    private String time;
    private String remark;
}
