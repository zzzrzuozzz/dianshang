package com.dianshang.admin.finance.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WithdrawApplyVO {
    private String applyNo;
    private Long userId;
    private String userNo;
    private String nickname;
    private String avatar;
    private String shopName;
    private String applyAmount;
    private String feeAmount;
    private String actualAmount;
    private String accountType;
    private String accountNo;
    private String bankName;
    private String holderName;
    private Integer verifyStatus;
    private String verifyStatusLabel;
    private String verifyUser;
    private String verifyTime;
    private String remark;
    private String createTime;
}
