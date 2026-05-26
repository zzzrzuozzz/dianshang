package com.dianshang.admin.ops.notification.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NotificationPageVO {
    private List<NotificationListVO> list;
    private long total;
    private int page;
    private int pageSize;
    private int totalPages;
}
