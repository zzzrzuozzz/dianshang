package com.dianshang.admin.member.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MemberOrderBriefVO {

    private String id;
    private String time;
    private BigDecimal amount;
    private String payMethod;
    private String source;
    private String status;
}
