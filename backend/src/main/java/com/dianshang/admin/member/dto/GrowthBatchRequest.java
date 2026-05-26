package com.dianshang.admin.member.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class GrowthBatchRequest {

    @NotEmpty
    private List<String> userIds;
    /** growth | points */
    private String adjustType;
    /** add | set */
    private String mode;
    private Integer value;
    private String remark;
}
