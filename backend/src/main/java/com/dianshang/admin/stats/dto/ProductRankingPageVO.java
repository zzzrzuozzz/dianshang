package com.dianshang.admin.stats.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ProductRankingPageVO {
    private List<RankingRowVO> list = new ArrayList<>();
    private long total;
    private int page;
    private int pageSize;

    @Getter
    @Setter
    public static class RankingRowVO {
        private String name;
        private int pv;
        private int uv;
        private int payUsers;
        private String conversion;
        private int salesQty;
        private String salesAmount;
    }
}
