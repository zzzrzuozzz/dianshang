package com.dianshang.admin.stats.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TransactionOverviewVO {
    private List<StatsKpiVO> kpis = new ArrayList<>();
    private TransactionTrendVO trend = new TransactionTrendVO();
    private PriceRangeVO priceRange = new PriceRangeVO();
    private List<NameValueVO> funnel = new ArrayList<>();
    private List<NameValueVO> userPie = new ArrayList<>();
    private UserSegmentVO newUser = new UserSegmentVO();
    private UserSegmentVO oldUser = new UserSegmentVO();
    private List<NameValueVO> sourcePie = new ArrayList<>();
    private List<SourceDetailVO> sourceDetail = new ArrayList<>();
    private DeviceSeriesVO deviceSeries = new DeviceSeriesVO();
    private FunnelRatesVO funnelRates = new FunnelRatesVO();

    @Getter
    @Setter
    public static class TransactionTrendVO {
        private List<String> dates = new ArrayList<>();
        private List<Number> paymentAmount = new ArrayList<>();
        private List<Number> refundAmount = new ArrayList<>();
        private List<Number> payUsers = new ArrayList<>();
        private List<Number> payOrders = new ArrayList<>();
        private List<Number> orderRate = new ArrayList<>();
        private List<Number> payRate = new ArrayList<>();
        private List<Number> dealRate = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class PriceRangeVO {
        private List<String> dates = new ArrayList<>();
        private List<String> ranges = new ArrayList<>();
        private List<List<Number>> data = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class UserSegmentVO {
        private String payAmount;
        private int payAmountTrend;
        private int payUsers;
        private int payUsersTrend;
    }

    @Getter
    @Setter
    public static class SourceDetailVO {
        private String name;
        private String amount;
        private int count;
        private int trend;
    }

    @Getter
    @Setter
    public static class DeviceSeriesVO {
        private List<String> dates = new ArrayList<>();
        private List<String> channels = new ArrayList<>();
        private List<List<Number>> series = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class FunnelRatesVO {
        private double orderRate;
        private double payRate;
        private double dealRate;
    }
}
