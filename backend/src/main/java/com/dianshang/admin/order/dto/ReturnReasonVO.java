package com.dianshang.admin.order.dto;

import lombok.Data;

@Data
public class ReturnReasonVO {

    private String id;
    private String reason;
    private String addTime;
    private Boolean visible;
    private Integer sort;
}
