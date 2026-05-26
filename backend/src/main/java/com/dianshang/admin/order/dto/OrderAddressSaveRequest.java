package com.dianshang.admin.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OrderAddressSaveRequest {

    @NotBlank(message = "地址类型不能为空")
    @Pattern(regexp = "ship|return", message = "地址类型无效")
    private String type;

    @NotBlank(message = "请输入联系人")
    @Size(max = 50)
    private String contactName;

    @NotBlank(message = "请输入联系电话")
    @Size(max = 20)
    private String phone;

    @Size(max = 30)
    private String province;

    @Size(max = 30)
    private String city;

    @Size(max = 30)
    private String district;

    @Size(max = 20)
    private String provinceCode;

    @Size(max = 20)
    private String cityCode;

    @Size(max = 20)
    private String districtCode;

    @NotBlank(message = "请输入详细地址")
    @Size(max = 300)
    private String detailAddress;

    @Size(max = 10)
    private String zipCode;

    private Boolean isDefault;
    private Boolean visible;
    private Integer sort;
}
