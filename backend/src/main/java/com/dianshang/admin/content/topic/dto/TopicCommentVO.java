package com.dianshang.admin.content.topic.dto;

import lombok.Data;

import java.util.List;

@Data
public class TopicCommentVO {

    private String id;
    private String content;
    private String replyContent;
    private List<String> pics;
    private Integer status;
    private String statusText;
}
