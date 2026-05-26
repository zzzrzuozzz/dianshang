package com.dianshang.admin.ops.advertisement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class AdvertisementSaveRequest {

    private String advCode;

    @NotBlank
    private String category;

    @NotBlank
    private String title;

    private String intro;
    private String jumpType;
    private String jumpUrl;
    private String detail;
    private List<String> coverImages;
    private List<String> memberLevels;
    private List<List<String>> regions;
    private Map<String, List<String>> tags;
    private String startTime;
    private String endTime;
    private Boolean online;
    private Boolean appPush;
    private Integer estimatedUsers;
}
