package com.dianshang.admin.permission.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MenuSaveRequest {

    private Long id;
    private Long parentId = 0L;

    @NotBlank(message = "菜单名称不能为空")
    private String menuName;

    @NotBlank(message = "菜单类型不能为空")
    private String menuType;

    private String path;
    private String perms;
    private String icon;
    private Integer sortNum = 0;
}
