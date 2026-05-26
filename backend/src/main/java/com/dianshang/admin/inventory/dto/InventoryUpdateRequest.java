package com.dianshang.admin.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class InventoryUpdateRequest {

    @NotBlank(message = "商品编号不能为空")
    private String goodsId;

    @NotEmpty(message = "请填写 SKU 库存")
    private List<InventorySkuVO> skus;
}
