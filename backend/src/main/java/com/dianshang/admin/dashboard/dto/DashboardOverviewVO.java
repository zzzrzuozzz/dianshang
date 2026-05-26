package com.dianshang.admin.dashboard.dto;

import lombok.Data;

import java.util.List;

@Data
public class DashboardOverviewVO {

    private List<StatCardVO> stats;
    private List<PendingTaskVO> pendingTasks;
    private List<QuickAccessVO> quickAccess;
    private ChartVO chart;

    @Data
    public static class StatCardVO {
        private String key;
        private String label;
        private Number value;
        private String displayValue;
        private int trend;
        private String compareLabel;
        private String iconKey;
        private String iconBg;
        private String iconColor;
    }

    @Data
    public static class PendingTaskVO {
        private String key;
        private String label;
        private int count;
    }

    @Data
    public static class QuickAccessVO {
        private String key;
        private String label;
        private String path;
        private String iconKey;
        private String iconBg;
        private String iconColor;
    }

    @Data
    public static class ChartVO {
        private List<String> dates;
        private List<Number> sales;
        private List<Number> orders;
    }
}
