package com.dianshang.admin.dashboard;

import com.dianshang.admin.dashboard.dto.DashboardOverviewVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardService {

    private final DashboardDailyMetricRepository metricRepository;
    private final DashboardPendingTaskRepository pendingTaskRepository;

    public DashboardService(DashboardDailyMetricRepository metricRepository,
                              DashboardPendingTaskRepository pendingTaskRepository) {
        this.metricRepository = metricRepository;
        this.pendingTaskRepository = pendingTaskRepository;
    }

    public DashboardOverviewVO getOverview() {
        List<DashboardDailyMetric> metrics = metricRepository.findAllByOrderByStatDateAsc();
        DashboardDailyMetric today = metrics.isEmpty() ? null : metrics.get(metrics.size() - 1);
        DashboardDailyMetric yesterday = metrics.size() >= 2 ? metrics.get(metrics.size() - 2) : today;

        DashboardOverviewVO vo = new DashboardOverviewVO();
        vo.setStats(buildStats(today, yesterday));
        vo.setPendingTasks(buildPending());
        vo.setQuickAccess(buildQuickAccess());
        vo.setChart(buildChart(metrics));
        return vo;
    }

    private List<DashboardOverviewVO.StatCardVO> buildStats(DashboardDailyMetric today, DashboardDailyMetric yesterday) {
        List<DashboardOverviewVO.StatCardVO> list = new ArrayList<>();
        if (today == null) {
            return list;
        }
        int yOrders = yesterday != null ? yesterday.getOrderCount() : 0;
        int yUsers = yesterday != null ? yesterday.getNewUserCount() : 0;
        int yPending = yesterday != null ? yesterday.getPendingPaymentCount() : 0;
        BigDecimal ySales = yesterday != null ? yesterday.getSalesAmount() : BigDecimal.ZERO;
        BigDecimal prevSales = yesterday != null ? yesterday.getYesterdaySalesAmount() : BigDecimal.ZERO;

        list.add(stat("todayOrders", "今日订单数", today.getOrderCount(), formatInt(today.getOrderCount()),
                trendPercent(today.getOrderCount(), yOrders), "较昨日", "Document", "#ecf5ff", "#409eff"));
        list.add(stat("todayNewUsers", "今日新增用户", today.getNewUserCount(), formatInt(today.getNewUserCount()),
                trendPercent(today.getNewUserCount(), yUsers), "较昨日", "User", "#f0f9eb", "#67c23a"));
        list.add(stat("pendingPayment", "待付款订单", today.getPendingPaymentCount(), formatInt(today.getPendingPaymentCount()),
                trendPercent(today.getPendingPaymentCount(), yPending), "较昨日", "CreditCard", "#fdf6ec", "#e6a23c"));
        list.add(stat("todaySales", "今日销售额", today.getSalesAmount(), "¥" + formatMoney(today.getSalesAmount()),
                trendPercent(today.getSalesAmount(), ySales), "较昨日", "Money", "#fef0f0", "#f56c6c"));
        list.add(stat("yesterdaySales", "昨日销售额", ySales, "¥" + formatMoney(ySales),
                trendPercent(ySales, prevSales), "较前日", "Wallet", "#f4f4f5", "#909399"));
        return list;
    }

    private DashboardOverviewVO.StatCardVO stat(String key, String label, Number value, String display,
                                                int trend, String compare, String iconKey, String iconBg, String iconColor) {
        DashboardOverviewVO.StatCardVO s = new DashboardOverviewVO.StatCardVO();
        s.setKey(key);
        s.setLabel(label);
        s.setValue(value);
        s.setDisplayValue(display);
        s.setTrend(trend);
        s.setCompareLabel(compare);
        s.setIconKey(iconKey);
        s.setIconBg(iconBg);
        s.setIconColor(iconColor);
        return s;
    }

    private List<DashboardOverviewVO.PendingTaskVO> buildPending() {
        return pendingTaskRepository.findAllByOrderBySortNumAsc().stream().map(t -> {
            DashboardOverviewVO.PendingTaskVO p = new DashboardOverviewVO.PendingTaskVO();
            p.setKey(t.getTaskKey());
            p.setLabel(t.getLabel());
            p.setCount(t.getCountValue());
            return p;
        }).toList();
    }

    private List<DashboardOverviewVO.QuickAccessVO> buildQuickAccess() {
        return List.of(
                quick("products", "商品管理", "/product/list", "Goods", "#ecf5ff", "#409eff"),
                quick("orders", "订单管理", "/order/list", "List", "#f0f9eb", "#67c23a"),
                quick("users", "用户管理", "/user/list", "User", "#fdf6ec", "#e6a23c"),
                quick("statistics", "交易统计", "/stats/transaction", "DataAnalysis", "#fef0f0", "#f56c6c"),
                quick("marketing", "广告管理", "/ops/advertisement", "Promotion", "#f4f4f5", "#909399"),
                quick("settings", "个人中心", "/profile/index", "Setting", "#ecf5ff", "#409eff")
        );
    }

    private DashboardOverviewVO.QuickAccessVO quick(String key, String label, String path,
                                                    String iconKey, String iconBg, String iconColor) {
        DashboardOverviewVO.QuickAccessVO q = new DashboardOverviewVO.QuickAccessVO();
        q.setKey(key);
        q.setLabel(label);
        q.setPath(path);
        q.setIconKey(iconKey);
        q.setIconBg(iconBg);
        q.setIconColor(iconColor);
        return q;
    }

    private DashboardOverviewVO.ChartVO buildChart(List<DashboardDailyMetric> metrics) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        DashboardOverviewVO.ChartVO chart = new DashboardOverviewVO.ChartVO();
        chart.setDates(metrics.stream().map(m -> m.getStatDate().format(fmt)).toList());
        chart.setSales(metrics.stream().map(m -> (Number) m.getSalesAmount()).toList());
        chart.setOrders(metrics.stream().map(m -> (Number) m.getOrderCount()).toList());
        return chart;
    }

    private int trendPercent(Number current, Number previous) {
        double c = current.doubleValue();
        double p = previous.doubleValue();
        if (p == 0) {
            return c > 0 ? 100 : 0;
        }
        return (int) Math.round((c - p) / p * 100);
    }

    private String formatInt(int n) {
        return new DecimalFormat("#,###").format(n);
    }

    private String formatMoney(BigDecimal amount) {
        return new DecimalFormat("#,##0.00").format(amount.setScale(2, RoundingMode.HALF_UP));
    }
}
