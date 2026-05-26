package com.dianshang.admin.profile.dto;

import lombok.Data;

@Data
public class ProfileVO {

    private String username;
    private String nickname;
    private String phone;
    private String email;
    private String gender;
    private String avatar;
    private String roleName;
    private String createTime;
}
