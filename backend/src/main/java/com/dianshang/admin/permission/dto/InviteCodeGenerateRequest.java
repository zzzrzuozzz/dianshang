package com.dianshang.admin.permission.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InviteCodeGenerateRequest {

    @NotNull(message = "请选择绑定角色")
    private Long roleId;

    @Min(value = 1, message = "至少生成1个")
    @Max(value = 50, message = "单次最多生成50个")
    private int count = 1;

    private String remark;
}
