package com.dianshang.admin.member.dto;

import lombok.Data;

@Data
public class GrowthTaskVO {

    private String taskCode;
    private String taskName;
    private Integer growthReward;
    private Integer pointsReward;
    private Boolean enabled;
    private String description;
    private Integer sortNum;
}
