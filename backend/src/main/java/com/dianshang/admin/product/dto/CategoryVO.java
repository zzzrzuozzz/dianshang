package com.dianshang.admin.product.dto;

import lombok.Data;

@Data
public class CategoryVO {

    private String id;
    private String name;
    private String level;
    private Integer count;
    private String unit;
    private Boolean visible;
    private Integer sort;
}
