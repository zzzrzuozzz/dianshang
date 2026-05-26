package com.dianshang.admin.inventory.dto;

import lombok.Data;

@Data
public class InventorySkuVO {

    private String skuName;
    private String skuId;
    private String skuCode;
    private Integer actualStock;
    private Integer warningStock;
    private String warehouseCode;
}
