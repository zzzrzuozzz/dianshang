package com.dianshang.admin.ops.notification.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "ops_notification")
public class OpsNotificationEntity {

    @Id
    @Column(name = "notify_code", nullable = false, length = 20)
    private String notifyCode;

    @Column(name = "msg_type", nullable = false, length = 20)
    private String msgType;

    @Column(name = "msg_category", length = 30)
    private String msgCategory;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 500)
    private String intro;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String content;

    @Column(name = "jump_type", length = 20)
    private String jumpType;

    @Column(name = "inner_link_type", length = 30)
    private String innerLinkType;

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

    @Column(name = "send_type", nullable = false)
    private Integer sendType = 1;

    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime;

    @Lob
    @Column(name = "generate_types_json", columnDefinition = "CLOB")
    private String generateTypesJson;

    @Column(name = "app_push", nullable = false)
    private Boolean appPush = false;

    @Column(name = "publish_status", nullable = false)
    private Integer publishStatus = 0;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "push_count", nullable = false)
    private Integer pushCount = 0;

    @Column(name = "push_volume", nullable = false)
    private Integer pushVolume = 0;

    @Column(name = "click_count", nullable = false)
    private Integer clickCount = 0;

    @Column(name = "receive_volume", nullable = false)
    private Integer receiveVolume = 0;

    @Column(name = "push_user_text", length = 200)
    private String pushUserText;

    @Column(name = "estimated_users", nullable = false)
    private Integer estimatedUsers = 0;

    @Column(nullable = false)
    private Boolean deleted = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    @PreUpdate
    void applyDefaults() {
        if (sendType == null) {
            sendType = 1;
        }
        if (appPush == null) {
            appPush = false;
        }
        if (publishStatus == null) {
            publishStatus = 0;
        }
        if (pushCount == null) {
            pushCount = 0;
        }
        if (pushVolume == null) {
            pushVolume = 0;
        }
        if (clickCount == null) {
            clickCount = 0;
        }
        if (receiveVolume == null) {
            receiveVolume = 0;
        }
        if (estimatedUsers == null) {
            estimatedUsers = 0;
        }
        if (deleted == null) {
            deleted = false;
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
