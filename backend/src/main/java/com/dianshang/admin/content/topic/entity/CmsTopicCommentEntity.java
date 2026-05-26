package com.dianshang.admin.content.topic.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "cms_topic_comment")
public class CmsTopicCommentEntity {

    @Id
    @Column(name = "comment_code", nullable = false, length = 20)
    private String commentCode;

    @Column(name = "topic_code", nullable = false, length = 20)
    private String topicCode;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(name = "reply_content", length = 500)
    private String replyContent;

    @Column(name = "pics_json", columnDefinition = "CLOB")
    private String picsJson;

    @Column(nullable = false)
    private Integer status = 0;

    @Column(nullable = false)
    private Boolean deleted = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
