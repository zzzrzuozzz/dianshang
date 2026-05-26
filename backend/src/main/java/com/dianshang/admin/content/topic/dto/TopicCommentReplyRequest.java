package com.dianshang.admin.content.topic.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TopicCommentReplyRequest {

    @NotBlank(message = "评论编号不能为空")
    private String commentCode;

    private String replyContent;
}
