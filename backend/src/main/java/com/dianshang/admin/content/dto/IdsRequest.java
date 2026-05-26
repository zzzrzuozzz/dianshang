package com.dianshang.admin.content.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class IdsRequest {

    @NotEmpty(message = "请选择至少一条记录")
    private List<String> ids;
}
