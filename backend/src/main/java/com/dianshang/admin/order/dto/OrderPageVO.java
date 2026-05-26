package com.dianshang.admin.order.dto;

import com.dianshang.admin.common.TabCountVO;
import lombok.Data;

import java.util.List;

@Data
public class OrderPageVO {

    private List<OrderListVO> list;
    private long total;
    private int page;
    private int pageSize;
    private int totalPages;
    private String activeTab;
    private List<TabCountVO> tabs;
}
