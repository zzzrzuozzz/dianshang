package com.dianshang.admin.permission.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleSaveRequest {

    private Long id;

    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    @NotBlank(message = "权限字符不能为空")
    private String roleKey;

    private Integer sortNum = 0;
    private Integer status = 1;
    private String remark;
}
