package com.dianshang.admin.content.help.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "cms_help_article")
public class CmsHelpArticleEntity {

    @Id
    @Column(name = "article_code", nullable = false, length = 20)
    private String articleCode;

    @Column(name = "type_id", nullable = false)
    private Long typeId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 500)
    private String intro;

    @Column(name = "content_html", columnDefinition = "CLOB")
    private String contentHtml;

    @Column(name = "cover_images_json", columnDefinition = "CLOB")
    private String coverImagesJson;

    @Column(name = "sort_num", nullable = false)
    private Integer sortNum = 0;

    @Column(nullable = false)
    private Integer status = 0;

    @Column(name = "click_count", nullable = false)
    private Integer clickCount = 0;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(nullable = false)
    private Boolean deleted = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
