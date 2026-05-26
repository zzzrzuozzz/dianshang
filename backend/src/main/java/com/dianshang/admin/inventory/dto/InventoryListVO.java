package com.dianshang.admin.inventory.dto;

import lombok.Data;

import java.util.List;

@Data
public class InventoryListVO {

    private String id;
    private String goodsId;
    private String thumb;
    private String name;
    private String skuName;
    private String skuId;
    private String category;
    private String supplier;
    private Integer actualStock;
    private Integer warningStock;
    private Integer frozenStock;
    private Integer availableStock;
    private String status;
    private String brand;
    private String expandTip;
    private List<InventorySkuVO> skus;
}
