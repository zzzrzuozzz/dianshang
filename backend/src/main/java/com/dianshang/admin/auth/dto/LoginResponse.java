package com.dianshang.admin.auth.dto;

import com.dianshang.admin.permission.dto.MenuTreeVO;
import lombok.Data;

import java.util.List;

@Data
public class LoginResponse {

    private String token;
    private String tokenType;
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private List<MenuTreeVO> menus;
    private List<String> roleKeys;
    private List<String> perms;

    public LoginResponse(String token, String tokenType, Long userId, String username,
                         String nickname, String avatar) {
        this.token = token;
        this.tokenType = tokenType;
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.avatar = avatar;
    }
}
