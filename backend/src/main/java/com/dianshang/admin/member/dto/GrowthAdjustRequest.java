package com.dianshang.admin.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GrowthAdjustRequest {

    @NotBlank
    private String userId;
    /** growth | points | settings */
    @NotBlank
    private String type;
    private Integer value;
    private String remark;
    private String level;
    private Integer points;
    private Integer growth;
}
