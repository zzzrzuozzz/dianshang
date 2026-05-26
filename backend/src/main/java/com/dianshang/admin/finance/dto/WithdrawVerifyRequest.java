package com.dianshang.admin.finance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WithdrawVerifyRequest {
    @NotBlank
    private String applyNo;
    @NotNull
    private Boolean passed;
    private String remark;
}
