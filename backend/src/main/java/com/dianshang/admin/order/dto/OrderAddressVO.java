package com.dianshang.admin.order.dto;

import lombok.Data;

@Data
public class OrderAddressVO {

    private String id;
    private String contactName;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String provinceCode;
    private String cityCode;
    private String districtCode;
    private String detailAddress;
    private String fullAddress;
    private String zipCode;
    private Boolean isDefault;
    private Boolean visible;
    private Integer sort;
    private String addTime;
}
