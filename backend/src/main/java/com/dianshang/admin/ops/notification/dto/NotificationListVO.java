package com.dianshang.admin.ops.notification.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationListVO {
    private String id;
    private String title;
    private Integer publishStatus;
    private String publishStatusText;
    private String appPush;
    private String publishTime;
    private String msgCategory;
    private String msgCategoryText;
    private Integer pushCount;
    private Integer pushVolume;
    private Integer clickCount;
    private Integer receiveVolume;
    private String pushUser;
}
