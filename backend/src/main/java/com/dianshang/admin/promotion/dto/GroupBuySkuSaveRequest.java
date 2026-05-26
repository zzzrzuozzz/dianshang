package com.dianshang.admin.promotion.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class GroupBuySkuSaveRequest {

    @NotBlank
    private String activityId;
    @NotBlank
    private String timeId;
    private List<GroupBuySkuVO> items;
}
