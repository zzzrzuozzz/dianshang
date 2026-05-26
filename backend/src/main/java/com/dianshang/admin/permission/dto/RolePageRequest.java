package com.dianshang.admin.permission.dto;

import lombok.Data;

@Data
public class RolePageRequest {

    private String keyword;
    private Integer status;
    private int page = 1;
    private int pageSize = 10;
}
