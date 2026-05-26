package com.dianshang.admin.ops.advertisement.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdvertisementListVO {
    private String id;
    private String name;
    private String publishTime;
    private String endTime;
    private String advType;
    private Integer exposureCount;
    private Integer clickCount;
    private Integer sort;
    private Integer status;
    private String statusText;
}
