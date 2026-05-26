package com.dianshang.admin.inventory.dto;

import com.dianshang.admin.common.TabCountVO;
import lombok.Data;

import java.util.List;

@Data
public class InventoryPageVO {

    private List<InventoryListVO> list;
    private long total;
    private int page;
    private int pageSize;
    private int totalPages;
    private List<TabCountVO> tabs;
}
