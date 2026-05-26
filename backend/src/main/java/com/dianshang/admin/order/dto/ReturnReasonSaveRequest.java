package com.dianshang.admin.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReturnReasonSaveRequest {

    @NotBlank(message = "请输入原因")
    @Size(max = 200)
    private String reason;

    private Boolean visible;
    private Integer sort;
}
