package com.dianshang.admin.system.dto;

import lombok.Data;

@Data
public class NoticeVO {

    private Long id;
    private String title;
    private String content;
    private String noticeType;
    private String noticeTypeLabel;
    private String level;
    private String levelLabel;
    private Integer status;
    private String sender;
    private String createTime;
}
