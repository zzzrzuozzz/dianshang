package com.dianshang.admin.profile.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ProfileUpdateRequest {

    @NotBlank(message = "请输入昵称")
    private String nickname;

    @NotBlank(message = "请输入手机号")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "请输入邮箱")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "请选择性别")
    private String gender;
}
