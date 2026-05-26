package com.dianshang.admin.system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "sys_notice")
public class SysNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "CLOB")
    private String content;

    @Column(name = "notice_type", length = 20)
    private String noticeType = "SYSTEM";

    @Column(length = 10)
    private String level = "INFO";

    private Integer status = 0;

    @Column(length = 50)
    private String sender = "系统通知";

    @Column(name = "create_time")
    private LocalDateTime createTime;
}
