package com.dianshang.admin.promotion.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PromoActivitySaveRequest {

    private String id;
    @NotBlank
    private String title;
    @NotBlank
    private String startTime;
    @NotBlank
    private String endTime;
    private Boolean online;
}
