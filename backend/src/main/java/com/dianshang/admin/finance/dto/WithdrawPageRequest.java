package com.dianshang.admin.finance.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WithdrawPageRequest {
    private String tab = "all";
    private String keyword;
    private int page = 1;
    private int pageSize = 10;
}
