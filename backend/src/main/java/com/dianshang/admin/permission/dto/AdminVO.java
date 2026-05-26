package com.dianshang.admin.permission.dto;

import lombok.Data;

import java.util.List;

@Data
public class AdminVO {

    private Long id;
    private String username;
    private String nickname;
    private Integer status;
    private String createTime;
    private List<RoleOptionVO> roles;
}
