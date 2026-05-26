package com.dianshang.admin.permission.dto;

import lombok.Data;

@Data
public class RoleVO {

    private Long id;
    private String roleName;
    private String roleKey;
    private Integer sortNum;
    private Integer status;
    private String remark;
    private String createTime;
}
