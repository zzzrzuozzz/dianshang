package com.dianshang.admin.ops.notification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class NotificationSaveRequest {

    private String notifyCode;

    @NotBlank
    private String msgType;

    private String category;
    private String title;
    private String intro;
    private String jumpType;
    private String innerLinkType;
    private String jumpUrl;
    private String detail;
    private List<String> coverImages;
    private List<String> memberLevels;
    private List<List<String>> regions;
    private Map<String, List<String>> tags;
    private List<String> pushMethod;
    private Integer sendType;
    private String publishTime;
    private List<String> generateTypes;
    private Boolean appPush;
    private String stationContent;
    private String smsContent;
    private Integer estimatedUsers;
}
