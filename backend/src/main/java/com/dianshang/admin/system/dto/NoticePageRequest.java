package com.dianshang.admin.system.dto;

import lombok.Data;

@Data
public class NoticePageRequest {

    /** SYSTEM | AUDIT | ALARM | 空=全部 */
    private String noticeType;
    private int page = 1;
    private int pageSize = 10;
}
