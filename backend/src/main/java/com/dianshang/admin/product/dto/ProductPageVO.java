package com.dianshang.admin.product.dto;

import com.dianshang.admin.common.TabCountVO;
import lombok.Data;

import java.util.List;

@Data
public class ProductPageVO {

    private List<ProductVO> list;
    private long total;
    private int page;
    private int pageSize;
    private List<TabCountVO> tabs;
}
