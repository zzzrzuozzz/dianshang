package com.dianshang.admin.member.dto;

import lombok.Data;

import java.util.Map;

@Data
public class MemberLevelVO {

    private String id;
    private String name;
    private Boolean isDefault;
    private Integer growthPoint;
    private String freeShipping;
    private String reviewReward;
    private Integer sort;
    private Integer freeShipAmount;
    private Integer freeShipTimes;
    private Integer reviewGrowth;
    private Integer reviewTimes;
    private Map<String, Boolean> privileges;
}
