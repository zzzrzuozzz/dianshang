package com.dianshang.admin.promotion.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TimeSlotSaveRequest {

    private String id;
    private String activityId;
    @NotBlank
    private String name;
    @NotBlank
    private String start;
    @NotBlank
    private String end;
    private Boolean enabled;
}
