package com.dianshang.admin.order.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class IdsRequest {

    @NotEmpty(message = "ids 不能为空")
    private List<String> ids;
}
