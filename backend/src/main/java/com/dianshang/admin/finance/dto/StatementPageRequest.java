package com.dianshang.admin.finance.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatementPageRequest {
    private String keyword;
    private String tradeType;
    private String paymentChannel;
    private String startTime;
    private String endTime;
    private int page = 1;
    private int pageSize = 10;
}
