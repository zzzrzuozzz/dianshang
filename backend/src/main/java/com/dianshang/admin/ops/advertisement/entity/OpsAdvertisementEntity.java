package com.dianshang.admin.ops.advertisement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "ops_advertisement")
public class OpsAdvertisementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "adv_code", nullable = false, unique = true, length = 20)
    private String advCode;

    @Column(name = "adv_type", nullable = false, length = 30)
    private String advType;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 500)
    private String intro;

    @Column(name = "jump_type", length = 20)
    private String jumpType;

    @Column(name = "jump_url", length = 500)
    private String jumpUrl;

    @Lob
    @Column(name = "detail_html", columnDefinition = "CLOB")
    private String detailHtml;

    @Lob
    @Column(name = "cover_images_json", columnDefinition = "CLOB")
    private String coverImagesJson;

    @Lob
    @Column(name = "audience_json", columnDefinition = "CLOB")
    private String audienceJson;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(nullable = false)
    private Boolean online = true;

    @Column(name = "app_push", nullable = false)
    private Boolean appPush = false;

    @Column(name = "exposure_count", nullable = false)
    private Integer exposureCount = 0;

    @Column(name = "click_count", nullable = false)
    private Integer clickCount = 0;

    @Column(name = "sort_num", nullable = false)
    private Integer sortNum = 0;

    @Column(nullable = false)
    private Boolean deleted = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
