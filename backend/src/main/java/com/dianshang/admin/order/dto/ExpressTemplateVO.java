package com.dianshang.admin.order.dto;

import lombok.Data;

@Data
public class ExpressTemplateVO {

    private String id;
    private String templateName;
    private String expressCompany;
    private String templateSpec;
    private String remark;
    private Boolean isDefault;
    private Boolean visible;
    private Integer sort;
    private String addTime;
}
