package com.dianshang.admin.member.dto;

import lombok.Data;

@Data
public class MemberLedgerVO {

    private String ledgerNo;
    private String userNo;
    private String nickname;
    private String account;
    private String ledgerType;
    private String changeType;
    private String changeTypeText;
    private Integer beforeQty;
    private Integer changeQty;
    private Integer afterQty;
    private String remark;
    private String operatorName;
    private String createdAt;
}
