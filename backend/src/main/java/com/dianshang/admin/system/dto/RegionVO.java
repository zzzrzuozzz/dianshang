package com.dianshang.admin.system.dto;

import lombok.Data;

@Data
public class RegionVO {

    private String code;
    private String name;
    private Integer level;
    private String parentCode;
    /** 前端懒加载树表：是否还有下级 */
    private Boolean hasChildren;
}
