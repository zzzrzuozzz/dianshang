package com.dianshang.admin.content.topic.dto;

import lombok.Data;

@Data
public class TopicTypeVO {

    private Long id;
    private String code;
    private String name;
    private String icon;
    private String intro;
    private Boolean visible;
    private Integer sort;
    private Integer topicCount;
}
