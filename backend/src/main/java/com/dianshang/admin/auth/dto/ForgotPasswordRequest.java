package com.dianshang.admin.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ForgotPasswordRequest {

    @NotBlank(message = "验证方式不能为空")
    @Pattern(regexp = "PHONE|EMAIL", message = "验证方式无效")
    private String accountType;

    @NotBlank(message = "注册时的手机号或邮箱不能为空")
    private String account;

    @NotBlank(message = "邀请码不能为空")
    private String inviteCode;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 64, message = "新密码不少于6个字符")
    private String newPassword;
}
