package com.dianshang.admin.finance.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionRecordVO {
    private String recordNo;
    private String orderNo;
    private String tradeType;
    private String tradeTypeLabel;
    private String amount;
    private String amountDisplay;
    private String paymentChannel;
    private String paymentChannelLabel;
    private Integer status;
    private String statusLabel;
    private String createTime;
}
