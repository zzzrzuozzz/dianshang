package com.dianshang.admin.product.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryTransferRequest {

    @NotBlank(message = "源分类不能为空")
    private String fromCategoryCode;

    @NotBlank(message = "目标分类不能为空")
    private String toCategoryCode;
}
