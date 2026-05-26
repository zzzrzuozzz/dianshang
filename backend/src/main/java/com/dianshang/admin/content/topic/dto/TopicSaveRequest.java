package com.dianshang.admin.content.topic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TopicSaveRequest {

    private String topicCode;

    @NotNull(message = "请选择专题类型")
    private Long typeId;

    @NotBlank(message = "请输入专题标题")
    private String title;

    private String intro;
    private String content;
    private String coverImage;
    private List<String> images;
    private Boolean specifyProducts;
    private List<String> productIds;
    private List<String> memberLevels;
    private List<List<String>> regions;
    private Map<String, List<String>> tags;
    private Integer sort;
    private Integer status;
}
