package com.dianshang.admin.content.topic.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "cms_topic")
public class CmsTopicEntity {

    @Id
    @Column(name = "topic_code", nullable = false, length = 20)
    private String topicCode;

    @Column(name = "type_id", nullable = false)
    private Long typeId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 500)
    private String intro;

    @Column(name = "content_html", columnDefinition = "CLOB")
    private String contentHtml;

    @Column(name = "cover_image", length = 500)
    private String coverImage;

    @Column(name = "images_json", columnDefinition = "CLOB")
    private String imagesJson;

    @Column(name = "audience_json", columnDefinition = "CLOB")
    private String audienceJson;

    @Column(name = "specify_products", nullable = false)
    private Boolean specifyProducts = false;

    @Column(name = "product_ids_json", columnDefinition = "CLOB")
    private String productIdsJson;

    @Column(name = "sort_num", nullable = false)
    private Integer sortNum = 0;

    @Column(nullable = false)
    private Integer status = 0;

    @Column(name = "click_count", nullable = false)
    private Integer clickCount = 0;

    @Column(name = "collect_count", nullable = false)
    private Integer collectCount = 0;

    @Column(name = "comment_count", nullable = false)
    private Integer commentCount = 0;

    @Column(name = "read_count", nullable = false)
    private Integer readCount = 0;

    @Column(name = "share_count", nullable = false)
    private Integer shareCount = 0;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(nullable = false)
    private Boolean deleted = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
