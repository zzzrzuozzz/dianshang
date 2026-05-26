package com.dianshang.admin.product.dto;

import lombok.Data;

import java.util.List;

@Data
public class CommentOverviewVO {

    private List<CommentStatCardVO> stats;

    @Data
    public static class CommentStatCardVO {
        private String key;
        private String label;
        private String value;
        private String action;
        private Integer todayNew;
        private Integer trend;
    }
}
