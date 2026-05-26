package com.dianshang.admin.content.topic.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TopicCommentReviewRequest {

    @NotBlank(message = "评论编号不能为空")
    private String commentCode;

    @NotBlank(message = "操作类型不能为空")
    private String action;
}
