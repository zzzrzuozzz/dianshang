package com.dianshang.admin.product.dto;

import lombok.Data;

@Data
public class AuditRequest {

    private Boolean passed;
    private String remark;
}
