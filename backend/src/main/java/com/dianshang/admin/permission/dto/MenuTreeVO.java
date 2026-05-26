package com.dianshang.admin.permission.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MenuTreeVO {

    private Long id;
    private Long parentId;
    private String menuName;
    private String menuType;
    private String path;
    private String perms;
    private String icon;
    private Integer sortNum;
    private List<MenuTreeVO> children = new ArrayList<>();
}
