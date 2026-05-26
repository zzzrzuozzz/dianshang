package com.dianshang.admin.content.topic.dto;

import lombok.Data;

@Data
public class TopicListVO {

    private String id;
    private Long typeId;
    private String typeName;
    private String title;
    private String publishTime;
    private Integer productCount;
    private Integer clickCount;
    private Integer collectCount;
    private Integer commentCount;
    private Integer sort;
    private Integer status;
    private String statusText;
}
