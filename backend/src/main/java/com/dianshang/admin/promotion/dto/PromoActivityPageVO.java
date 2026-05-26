package com.dianshang.admin.promotion.dto;

import lombok.Data;

import java.util.List;

@Data
public class PromoActivityPageVO {

    private List<PromoActivityVO> list;
    private long total;
    private int page;
    private int pageSize;
    private int totalPages;
}
