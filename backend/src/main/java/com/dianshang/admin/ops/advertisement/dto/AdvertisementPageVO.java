package com.dianshang.admin.ops.advertisement.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AdvertisementPageVO {
    private List<AdvertisementListVO> list;
    private long total;
    private int page;
    private int pageSize;
    private int totalPages;
}
