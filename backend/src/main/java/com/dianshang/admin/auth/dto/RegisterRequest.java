package com.dianshang.admin.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "注册方式不能为空")
    @Pattern(regexp = "PHONE|EMAIL", message = "注册方式无效")
    private String accountType;

    @NotBlank(message = "手机号或邮箱不能为空")
    private String account;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 6, max = 50, message = "用户名不少于6个字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码不少于6个字符")
    private String password;

    private String nickname;

    @NotBlank(message = "邀请码不能为空")
    private String inviteCode;
}
