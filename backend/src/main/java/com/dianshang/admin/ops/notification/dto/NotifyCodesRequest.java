package com.dianshang.admin.ops.notification.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class NotifyCodesRequest {

    @NotEmpty(message = "请选择至少一条记录")
    private List<String> ids;
}
