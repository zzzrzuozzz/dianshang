package com.dianshang.admin.product.dto;

import lombok.Data;

@Data
public class BrandVO {

    private String id;
    private String name;
    private String initial;
    private Integer count;
    private String supplier;
    private Boolean visible;
    private Integer sort;
}
