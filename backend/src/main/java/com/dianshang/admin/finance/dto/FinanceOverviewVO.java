package com.dianshang.admin.finance.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class FinanceOverviewVO {
    private List<FinanceKpiVO> kpis = new ArrayList<>();
    private FinanceChartVO chart = new FinanceChartVO();
    private List<WithdrawBriefVO> pendingWithdraws = new ArrayList<>();

    @Getter
    @Setter
    public static class FinanceKpiVO {
        private String key;
        private String label;
        private String value;
        private String display;
        private int trend;
        private String tag;
        private String tagType;
        private String iconKey;
        private String iconBg;
        private String iconColor;
    }

    @Getter
    @Setter
    public static class FinanceChartVO {
        private String granularity = "day";
        private List<String> dates = new ArrayList<>();
        private List<Number> incomeSeries = new ArrayList<>();
        private List<Number> refundSeries = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class WithdrawBriefVO {
        private String applyNo;
        private String memberName;
        private String applyAmount;
        private String createTime;
    }
}
