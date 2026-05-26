package com.dianshang.admin.order.dto;

import com.dianshang.admin.common.TabCountVO;
import lombok.Data;

import java.util.List;

@Data
public class AfterSalePageVO {

    private List<AfterSaleListVO> list;
    private long total;
    private int page;
    private int pageSize;
    private int totalPages;
    private List<TabCountVO> tabs;
}
