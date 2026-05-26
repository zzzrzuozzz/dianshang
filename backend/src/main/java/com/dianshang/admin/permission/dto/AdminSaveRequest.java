package com.dianshang.admin.permission.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class AdminSaveRequest {

    private Long id;

    @NotBlank(message = "登录账号不能为空")
    private String username;

    private String password;

    private String nickname;
    private Integer status = 1;
    private List<Long> roleIds;
}
