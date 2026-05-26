package com.dianshang.admin.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class MemberTagSaveRequest {

    private String id;
    @NotBlank
    private String name;
    private List<String> gender;
    private List<String> memberLevels;
    private String cityMode;
    private List<List<String>> regionCodes;
    private Boolean registerEnabled;
    private String registerType;
    private List<String> registerRange;
    private Integer registerDays;
    private Boolean orderCountEnabled;
    private String orderCountType;
    private Integer orderCount;
    private Boolean amountEnabled;
    private String amountType;
    private Double amount;
}
