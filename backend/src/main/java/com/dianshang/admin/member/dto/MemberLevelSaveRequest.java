package com.dianshang.admin.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class MemberLevelSaveRequest {

    private String id;
    @NotBlank
    private String name;
    private Integer growthPoint;
    private Boolean isDefault;
    private Integer freeShipAmount;
    private Integer freeShipTimes;
    private Integer reviewGrowth;
    private Integer reviewTimes;
    private Map<String, Boolean> privileges;
}
