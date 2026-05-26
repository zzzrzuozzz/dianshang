package com.dianshang.admin.promotion.dto;

import lombok.Data;

@Data
public class TimeSlotVO {

    private String id;
    private String name;
    private String start;
    private String end;
    private Boolean enabled;
}
