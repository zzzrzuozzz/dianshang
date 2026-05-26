package com.dianshang.admin.stats.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ProductCategoryPageVO {
    private List<CategoryRowVO> list = new ArrayList<>();
    private List<NameValueVO> pie = new ArrayList<>();
    private long total;
    private int page;
    private int pageSize;

    @Getter
    @Setter
    public static class CategoryRowVO {
        private String name;
        private int qty;
        private String qtyRate;
        private String amount;
        private String amountRate;
    }
}
