package com.dianshang.admin.ops.notification.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class AudienceEstimateRequest {
    private List<String> memberLevels;
    private List<List<String>> regions;
    private Map<String, List<String>> tags;
}
