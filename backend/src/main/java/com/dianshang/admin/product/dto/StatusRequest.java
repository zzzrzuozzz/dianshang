package com.dianshang.admin.product.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StatusRequest {

    @NotBlank(message = "status 不能为空")
    private String status;
}
