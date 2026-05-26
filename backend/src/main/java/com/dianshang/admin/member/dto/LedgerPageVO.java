package com.dianshang.admin.member.dto;

import lombok.Data;

import java.util.List;

@Data
public class LedgerPageVO {

    private List<MemberLedgerVO> list;
    private long total;
    private int page;
    private int pageSize;
    private int totalPages;
}
