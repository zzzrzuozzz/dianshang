package com.dianshang.admin.content.topic.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TopicTypeSaveRequest {

    private Long id;

    @NotBlank(message = "请输入类型名称")
    private String name;

    private Integer sort;

    private Boolean visible;

    private String icon;

    private String intro;
}
