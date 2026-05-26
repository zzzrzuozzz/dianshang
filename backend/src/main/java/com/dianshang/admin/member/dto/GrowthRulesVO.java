package com.dianshang.admin.member.dto;

import lombok.Data;

@Data
public class GrowthRulesVO {

    private Integer orderGrowthPerYuan;
    private Integer orderPointsPerYuan;
    private Integer signInGrowth;
    private Integer signInPoints;
    private Integer reviewGrowth;
    private Integer reviewPoints;
    private String growthExpireDays;
    private String pointsExpireDays;
}
