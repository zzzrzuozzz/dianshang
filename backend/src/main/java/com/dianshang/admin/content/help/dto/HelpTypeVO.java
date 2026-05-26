package com.dianshang.admin.content.help.dto;

import lombok.Data;

@Data
public class HelpTypeVO {

    private Long id;
    private String code;
    private String name;
    private String icon;
    private Boolean visible;
    private Integer sort;
    private Integer articleCount;
}
