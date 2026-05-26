package com.dianshang.admin.system.dto;

import lombok.Data;

@Data
public class NoticeUnreadSummaryVO {

    private long total;
    private long systemCount;
    private long auditCount;
    private long alarmCount;
    private long urgentCount;
}
