package com.dianshang.admin.member.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MemberListVO {

    private String id;
    private String nickname;
    private String account;
    private String level;
    private String levelKey;
    private BigDecimal consumeAmount;
    private Integer orderCount;
    private Integer points;
    private Integer growth;
    private String status;
    private String statusText;
    private String remark;
    private String registerTime;
    private String avatar;
}
