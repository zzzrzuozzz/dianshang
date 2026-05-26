package com.dianshang.admin.permission.dto;

import lombok.Data;

@Data
public class AdminPageRequest {

    private String username;
    private String nickname;
    private int page = 1;
    private int pageSize = 10;
}
