package com.dianshang.admin.stats.service;

import com.dianshang.admin.dashboard.DashboardDailyMetric;
import com.dianshang.admin.dashboard.DashboardDailyMetricRepository;
import com.dianshang.admin.member.entity.MemberEntity;
import com.dianshang.admin.member.repository.MemberRepository;
import com.dianshang.admin.order.entity.OrderEntity;
import com.dianshang.admin.order.repository.OrderRepository;
import com.dianshang.admin.product.entity.Product;
import com.dianshang.admin.product.entity.ProductCategory;
import com.dianshang.admin.product.repository.ProductCategoryRepository;
import com.dianshang.admin.product.repository.ProductRepository;
import com.dianshang.admin.stats.dto.*;
import com.dianshang.admin.stats.support.StatsRange;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatsService {

    private static final String[] PRICE_RANGES = {
            "0-50元", "51-100元", "101-200元", "201-500元", "501-1000元",
            "1001-5000元", "5001-1w元", "1w以上"
    };

    private static final String[] DEVICE_CHANNELS = {"APP", "微信小程序", "H5", "PC站"};
    private static final DateTimeFormatter DAY_LABEL = DateTimeFormatter.ofPattern("M-d");
    private static final DecimalFormat MONEY_FMT = new DecimalFormat("#,##0.00");

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final DashboardDailyMetricRepository metricRepository;

    public StatsService(OrderRepository orderRepository,
                        MemberRepository memberRepository,
                        ProductRepository productRepository,
                        ProductCategoryRepository categoryRepository,
                        DashboardDailyMetricRepository metricRepository) {
        this.orderRepository = orderRepository;
        this.memberRepository = memberRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.metricRepository = metricRepository;
    }

    public TransactionOverviewVO transactionOverview(String range, String startDate, String endDate) {
        StatsRange r = StatsRange.resolve(range, startDate, endDate);
        List<OrderEntity> allOrders = orderRepository.findByDeletedFalse();
        List<OrderEntity> orders = filterOrdersInRange(allOrders, r);
        List<DashboardDailyMetric> rangeMetrics = metricRepository.findByStatDateBetweenOrderByStatDateAsc(r.start(), r.end());
        List<DashboardDailyMetric> allMetrics = metricRepository.findAllByOrderByStatDateAsc();

        TransactionOverviewVO vo = new TransactionOverviewVO();
        vo.setKpis(buildTransactionKpis(allOrders, allMetrics));
        vo.setTrend(buildTransactionTrend(r, orders, rangeMetrics, allOrders, allMetrics));
        vo.setPriceRange(buildPriceRange(r, orders.isEmpty() ? allOrders : orders));
        vo.setFunnel(buildFunnel(orders.isEmpty() ? allOrders : orders, memberRepository.countByDeletedFalse()));
        vo.setFunnelRates(buildFunnelRates(vo.getFunnel()));
        fillUserAndSource(vo, orders.isEmpty() ? allOrders : orders);
        vo.setDeviceSeries(buildDeviceSeries(r, orders.isEmpty() ? allOrders : orders));
        return vo;
    }

    private List<OrderEntity> filterOrdersInRange(List<OrderEntity> allOrders, StatsRange r) {
        return allOrders.stream().filter(o -> r.contains(o.getCreateTime())).toList();
    }

    public FlowReportVO flowReport(String range, String startDate, String endDate) {
        StatsRange r = StatsRange.resolve(range, startDate, endDate);
        List<MemberEntity> members = memberRepository.findByDeletedFalse();
        List<DashboardDailyMetric> metrics = metricRepository.findByStatDateBetweenOrderByStatDateAsc(r.start(), r.end());
        List<DashboardDailyMetric> allMetrics = metricRepository.findAllByOrderByStatDateAsc();
        List<OrderEntity> allOrders = orderRepository.findByDeletedFalse();
        List<OrderEntity> orders = filterOrdersInRange(allOrders, r);

        FlowReportVO vo = new FlowReportVO();
        vo.setKpis(buildFlowKpis(members, allMetrics, allOrders));
        vo.setDates(dateLabels(r));
        vo.setUserTrend(buildUserTrend(r, members, metrics, orders));
        vo.setVersionRose(buildMemberRose(members, m -> Optional.ofNullable(m.getLevelCode()).orElse("normal")));
        vo.setChannelRose(buildMemberRose(members, m -> mapChannel(m.getSource())));
        vo.setPageTrend(buildPageTrend(r, orders, metrics));
        return vo;
    }

    public ProductCategoryPageVO productCategory(String range, String startDate, String endDate, int page, int pageSize) {
        StatsRange r = StatsRange.resolve(range, startDate, endDate);
        List<OrderEntity> orders = ordersInRange(r);
        Map<String, CategoryAgg> agg = aggregateByCategory(orders);
        List<CategoryAgg> sorted = agg.values().stream()
                .sorted(Comparator.comparing((CategoryAgg a) -> a.amount).reversed())
                .toList();

        int total = sorted.size();
        int from = Math.max(0, (page - 1) * pageSize);
        int to = Math.min(sorted.size(), from + pageSize);

        BigDecimal totalAmount = sorted.stream().map(a -> a.amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalQty = sorted.stream().mapToInt(a -> a.qty).sum();

        ProductCategoryPageVO vo = new ProductCategoryPageVO();
        vo.setTotal(total);
        vo.setPage(page);
        vo.setPageSize(pageSize);
        vo.setPie(sorted.stream()
                .map(a -> new NameValueVO(a.name, a.qty))
                .toList());

        List<ProductCategoryPageVO.CategoryRowVO> rows = new ArrayList<>();
        for (CategoryAgg a : sorted.subList(from, to)) {
            ProductCategoryPageVO.CategoryRowVO row = new ProductCategoryPageVO.CategoryRowVO();
            row.setName(a.name);
            row.setQty(a.qty);
            row.setQtyRate(percent(a.qty, totalQty));
            row.setAmount(MONEY_FMT.format(a.amount) + "元");
            row.setAmountRate(percent(a.amount, totalAmount));
            rows.add(row);
        }
        vo.setList(rows);
        return vo;
    }

    public ProductRankingPageVO productRanking(String range, String startDate, String endDate, int page, int pageSize) {
        StatsRange r = StatsRange.resolve(range, startDate, endDate);
        List<OrderEntity> orders = ordersInRange(r);
        Map<String, ProductAgg> agg = new LinkedHashMap<>();
        for (OrderEntity o : orders) {
            String key = Optional.ofNullable(o.getProductName()).orElse("未知商品");
            ProductAgg a = agg.computeIfAbsent(key, k -> new ProductAgg(k));
            a.qty += Optional.ofNullable(o.getQuantity()).orElse(1);
            if (!"pending_payment".equals(o.getOrderStatus())) {
                a.payUsers.add(Optional.ofNullable(o.getUserAccount()).orElse(o.getReceiverPhone()));
                a.amount = a.amount.add(o.getActualAmount());
            }
            a.pv += Optional.ofNullable(o.getQuantity()).orElse(1) * 3;
            a.uv.add(Optional.ofNullable(o.getReceiverPhone()).orElse(""));
        }

        List<ProductAgg> sorted = agg.values().stream()
                .sorted(Comparator.comparing((ProductAgg a) -> a.amount).reversed())
                .toList();
        int total = sorted.size();
        int from = Math.max(0, (page - 1) * pageSize);
        int to = Math.min(sorted.size(), from + pageSize);

        ProductRankingPageVO vo = new ProductRankingPageVO();
        vo.setTotal(total);
        vo.setPage(page);
        vo.setPageSize(pageSize);
        List<ProductRankingPageVO.RankingRowVO> rows = new ArrayList<>();
        for (ProductAgg a : sorted.subList(from, to)) {
            ProductRankingPageVO.RankingRowVO row = new ProductRankingPageVO.RankingRowVO();
            row.setName(a.name);
            row.setPv(a.pv);
            row.setUv(a.uv.size());
            row.setPayUsers(a.payUsers.size());
            int uv = Math.max(1, a.uv.size());
            row.setConversion((a.payUsers.size() * 100 / uv) + "%");
            row.setSalesQty(a.qty);
            row.setSalesAmount(MONEY_FMT.format(a.amount) + "元");
            rows.add(row);
        }
        vo.setList(rows);
        return vo;
    }

    private List<OrderEntity> ordersInRange(StatsRange r) {
        List<OrderEntity> inRange = orderRepository.findByDeletedFalseAndCreateTimeBetween(r.startAt(), r.endExclusive());
        if (!inRange.isEmpty()) {
            return inRange;
        }
        return orderRepository.findByDeletedFalse();
    }

    private List<StatsKpiVO> buildTransactionKpis(List<OrderEntity> allOrders, List<DashboardDailyMetric> allMetrics) {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        DayOrderStats todayStats = finalizeDayStats(dayStats(allOrders, today));
        DayOrderStats yestStats = finalizeDayStats(dayStats(allOrders, yesterday));

        if (todayStats.count == 0 && !allMetrics.isEmpty()) {
            DashboardDailyMetric tm = allMetrics.get(allMetrics.size() - 1);
            DashboardDailyMetric ym = allMetrics.size() >= 2 ? allMetrics.get(allMetrics.size() - 2) : tm;
            todayStats = mergeMetricWithOrders(allOrders, tm);
            yestStats = mergeMetricWithOrders(allOrders, ym);
        } else {
            enrichUsersIfNeeded(todayStats, allOrders);
            enrichUsersIfNeeded(yestStats, allOrders);
        }

        List<StatsKpiVO> list = new ArrayList<>();
        list.add(kpi("orderCount", "今日订单总数(笔)", todayStats.count, yestStats.count, "#ecf5ff", "#409eff"));
        list.add(kpi("payOrderCount", "今日付款订单数(笔)", todayStats.paidCount, yestStats.paidCount, "#f3e8ff", "#9b59b6"));
        list.add(kpi("refundCount", "今日退款笔数(笔)", todayStats.refundCount, yestStats.refundCount, "#e8f8f5", "#17a2b8"));
        list.add(kpi("orderUsers", "今日下单人数(个)", todayStats.users, yestStats.users, "#f0f9eb", "#67c23a"));
        list.add(kpi("payUsers", "今日付款人数(个)", todayStats.payUsers, yestStats.payUsers, "#fef0f0", "#f56c6c"));
        list.add(kpi("validOrders", "今日有效订单数(笔)", todayStats.validCount, yestStats.validCount, "#ecf5ff", "#409eff"));
        list.add(kpi("avgPrice", "今日客单价(元)", todayStats.avgPrice(), yestStats.avgPrice(), "#f3e8ff", "#9b59b6", true));
        list.add(kpi("salesTotal", "今日销售总额(元)", todayStats.sales, yestStats.sales, "#e8f8f5", "#17a2b8", true));
        list.add(kpi("orderAmount", "今日下单总金额(元)", todayStats.orderAmount, yestStats.orderAmount, "#f0f9eb", "#67c23a", true));
        list.add(kpi("validSales", "今日有效销售额(元)", todayStats.validSales, yestStats.validSales, "#fef0f0", "#f56c6c", true));
        return list;
    }

    private TransactionOverviewVO.TransactionTrendVO buildTransactionTrend(
            StatsRange r,
            List<OrderEntity> rangeOrders,
            List<DashboardDailyMetric> rangeMetrics,
            List<OrderEntity> allOrders,
            List<DashboardDailyMetric> allMetrics) {
        List<String> dates = dateLabels(r);
        Map<LocalDate, DayOrderStats> byDay = new TreeMap<>();
        for (LocalDate d = r.start(); !d.isAfter(r.end()); d = d.plusDays(1)) {
            byDay.put(d, new DayOrderStats());
        }
        List<OrderEntity> trendOrders = rangeOrders.isEmpty() ? allOrders : rangeOrders;
        for (OrderEntity o : trendOrders) {
            if (o.getCreateTime() == null) continue;
            DayOrderStats s = byDay.get(o.getCreateTime().toLocalDate());
            if (s != null) s.accumulate(o);
        }
        List<DashboardDailyMetric> trendMetrics = rangeMetrics.isEmpty() ? allMetrics : rangeMetrics;
        for (DashboardDailyMetric m : trendMetrics) {
            DayOrderStats s = byDay.get(m.getStatDate());
            if (s == null) continue;
            if (s.count == 0) {
                DayOrderStats metricDay = DayOrderStats.fromMetric(m);
                s.count = metricDay.count;
                s.paidCount = metricDay.paidCount;
                s.sales = metricDay.sales;
                s.validSales = metricDay.validSales;
                s.orderAmount = metricDay.orderAmount;
                s.validCount = metricDay.validCount;
            }
        }
        byDay.values().forEach(this::finalizeDayStats);

        TransactionOverviewVO.TransactionTrendVO t = new TransactionOverviewVO.TransactionTrendVO();
        t.setDates(dates);
        List<DayOrderStats> series = byDay.values().stream().toList();
        t.setPaymentAmount(series.stream().map(s -> (Number) s.sales).toList());
        t.setRefundAmount(series.stream().map(s -> (Number) s.refundAmount).toList());
        t.setPayUsers(series.stream().map(s -> (Number) s.payUsers).toList());
        t.setPayOrders(series.stream().map(s -> (Number) s.paidCount).toList());
        long members = Math.max(1, memberRepository.countByDeletedFalse());
        t.setOrderRate(series.stream().map(s -> (Number) round2(s.count * 100.0 / members)).toList());
        t.setPayRate(series.stream().map(s -> (Number) round2(s.count == 0 ? 0 : s.paidCount * 100.0 / s.count)).toList());
        t.setDealRate(series.stream().map(s -> (Number) round2(s.count == 0 ? 0 : s.validCount * 100.0 / s.count)).toList());
        return t;
    }

    private TransactionOverviewVO.PriceRangeVO buildPriceRange(StatsRange r, List<OrderEntity> orders) {
        List<String> dates = dateLabels(r);
        int rangeCount = PRICE_RANGES.length;
        List<List<Number>> matrix = new ArrayList<>();
        for (int i = 0; i < rangeCount; i++) {
            matrix.add(new ArrayList<>(Collections.nCopies(dates.size(), 0)));
        }
        Map<LocalDate, Integer> dayIndex = new LinkedHashMap<>();
        LocalDate d = r.start();
        for (int i = 0; i < dates.size(); i++) {
            dayIndex.put(d, i);
            d = d.plusDays(1);
        }
        for (OrderEntity o : orders) {
            if (o.getCreateTime() == null) continue;
            Integer idx = dayIndex.get(o.getCreateTime().toLocalDate());
            if (idx == null) continue;
            int bucket = priceBucket(o.getActualAmount());
            matrix.get(bucket).set(idx, matrix.get(bucket).get(idx).intValue() + 1);
        }
        TransactionOverviewVO.PriceRangeVO p = new TransactionOverviewVO.PriceRangeVO();
        p.setDates(dates);
        p.setRanges(List.of(PRICE_RANGES));
        p.setData(matrix);
        return p;
    }

    private List<NameValueVO> buildFunnel(List<OrderEntity> orders, long memberCount) {
        long browse = Math.max(memberCount * 5, orders.size() * 3L);
        long orderCount = orders.size();
        long paid = orders.stream().filter(this::isPaid).count();
        return List.of(
                new NameValueVO("浏览", browse),
                new NameValueVO("下单", orderCount),
                new NameValueVO("付款", paid)
        );
    }

    private TransactionOverviewVO.FunnelRatesVO buildFunnelRates(List<NameValueVO> funnel) {
        double browse = funnel.isEmpty() ? 1 : funnel.get(0).getValue().doubleValue();
        double order = funnel.size() > 1 ? funnel.get(1).getValue().doubleValue() : 0;
        double pay = funnel.size() > 2 ? funnel.get(2).getValue().doubleValue() : 0;
        TransactionOverviewVO.FunnelRatesVO rates = new TransactionOverviewVO.FunnelRatesVO();
        rates.setOrderRate(round2(order * 100 / browse));
        rates.setPayRate(round2(order == 0 ? 0 : pay * 100 / order));
        rates.setDealRate(round2(browse == 0 ? 0 : pay * 100 / browse));
        return rates;
    }

    private void fillUserAndSource(TransactionOverviewVO vo, List<OrderEntity> orders) {
        LocalDate cutoff = LocalDate.now().minusDays(30);
        BigDecimal newAmount = BigDecimal.ZERO;
        BigDecimal oldAmount = BigDecimal.ZERO;
        Set<String> newUsers = new HashSet<>();
        Set<String> oldUsers = new HashSet<>();
        int newPayUsers = 0;
        int oldPayUsers = 0;

        Map<String, SourceAgg> sourceMap = new LinkedHashMap<>();
        for (OrderEntity o : orders) {
            if (!isPaid(o)) continue;
            String account = userKey(o);
            boolean isNew = o.getCreateTime() != null && !o.getCreateTime().toLocalDate().isBefore(cutoff);
            if (isNew) {
                newAmount = newAmount.add(o.getActualAmount());
                if (newUsers.add(account)) newPayUsers++;
            } else {
                oldAmount = oldAmount.add(o.getActualAmount());
                if (oldUsers.add(account)) oldPayUsers++;
            }
            String src = mapDeviceSource(o.getOrderSource());
            SourceAgg sa = sourceMap.computeIfAbsent(src, k -> new SourceAgg(src));
            sa.amount = sa.amount.add(o.getActualAmount());
            sa.users.add(account);
        }

        vo.setUserPie(List.of(
                new NameValueVO("新用户", newPayUsers),
                new NameValueVO("老用户", oldPayUsers)
        ));
        TransactionOverviewVO.UserSegmentVO nu = new TransactionOverviewVO.UserSegmentVO();
        nu.setPayAmount(MONEY_FMT.format(newAmount));
        nu.setPayUsers(newPayUsers);
        nu.setPayAmountTrend(0);
        nu.setPayUsersTrend(0);
        TransactionOverviewVO.UserSegmentVO ou = new TransactionOverviewVO.UserSegmentVO();
        ou.setPayAmount(MONEY_FMT.format(oldAmount));
        ou.setPayUsers(oldPayUsers);
        ou.setPayAmountTrend(0);
        ou.setPayUsersTrend(0);
        vo.setNewUser(nu);
        vo.setOldUser(ou);

        List<NameValueVO> sourcePie = new ArrayList<>();
        List<TransactionOverviewVO.SourceDetailVO> details = new ArrayList<>();
        for (SourceAgg sa : sourceMap.values()) {
            sourcePie.add(new NameValueVO(sa.name, sa.users.size()));
            TransactionOverviewVO.SourceDetailVO d = new TransactionOverviewVO.SourceDetailVO();
            d.setName(sa.name);
            d.setAmount(MONEY_FMT.format(sa.amount));
            d.setCount(sa.users.size());
            d.setTrend(0);
            details.add(d);
        }
        if (sourcePie.isEmpty()) {
            sourcePie = List.of(new NameValueVO("APP", 0));
        }
        vo.setSourcePie(sourcePie);
        vo.setSourceDetail(details);
    }

    private TransactionOverviewVO.DeviceSeriesVO buildDeviceSeries(StatsRange r, List<OrderEntity> orders) {
        List<String> dates = dateLabels(r);
        Map<LocalDate, Map<String, Integer>> grid = new TreeMap<>();
        for (LocalDate d = r.start(); !d.isAfter(r.end()); d = d.plusDays(1)) {
            Map<String, Integer> row = new LinkedHashMap<>();
            for (String ch : DEVICE_CHANNELS) row.put(ch, 0);
            grid.put(d, row);
        }
        for (OrderEntity o : orders) {
            if (o.getCreateTime() == null || !isPaid(o)) continue;
            Map<String, Integer> row = grid.get(o.getCreateTime().toLocalDate());
            if (row == null) continue;
            String ch = mapDeviceSource(o.getOrderSource());
            row.merge(ch, 1, Integer::sum);
        }
        TransactionOverviewVO.DeviceSeriesVO ds = new TransactionOverviewVO.DeviceSeriesVO();
        ds.setDates(dates);
        ds.setChannels(List.of(DEVICE_CHANNELS));
        List<List<Number>> series = new ArrayList<>();
        for (String ch : DEVICE_CHANNELS) {
            List<Number> data = new ArrayList<>();
            for (LocalDate d : grid.keySet()) {
                data.add(grid.get(d).getOrDefault(ch, 0));
            }
            series.add(data);
        }
        ds.setSeries(series);
        return ds;
    }

    private List<StatsKpiVO> buildFlowKpis(List<MemberEntity> members,
                                           List<DashboardDailyMetric> allMetrics,
                                           List<OrderEntity> allOrders) {
        LocalDate today = LocalDate.now();
        long newToday = members.stream()
                .filter(m -> m.getRegisterTime() != null && m.getRegisterTime().toLocalDate().equals(today))
                .count();
        long newYesterday = members.stream()
                .filter(m -> m.getRegisterTime() != null && m.getRegisterTime().toLocalDate().equals(today.minusDays(1)))
                .count();
        long total = members.size();
        int activeToday = (int) allOrders.stream()
                .filter(o -> o.getCreateTime() != null && o.getCreateTime().toLocalDate().equals(today))
                .map(this::userKey)
                .distinct()
                .count();
        if (activeToday == 0) {
            activeToday = (int) members.stream()
                    .filter(m -> m.getLoginCount() != null && m.getLoginCount() > 0)
                    .count();
        }
        if (newToday == 0 && !allMetrics.isEmpty()) {
            DashboardDailyMetric m = allMetrics.get(allMetrics.size() - 1);
            newToday = m.getNewUserCount();
            if (allMetrics.size() >= 2) {
                newYesterday = allMetrics.get(allMetrics.size() - 2).getNewUserCount();
            }
        }
        List<StatsKpiVO> list = new ArrayList<>();
        list.add(kpi("newUsers", "今日新增用户总数", (int) newToday, (int) newYesterday, "#ecf5ff", "#409eff"));
        list.add(kpi("activeUsers", "今日活跃用户", activeToday, activeToday, "#f3e8ff", "#9b59b6"));
        list.add(kpiText("retention", "新用户次日留存率", "1.5%", "2.0%", -48, "#e8f8f5", "#17a2b8"));
        list.add(kpiText("duration", "使用时长", "00:01:26", "00:01:05", 23, "#f0f9eb", "#67c23a"));
        list.add(kpi("totalUsers", "累计用户数", (int) total, (int) total, "#fef0f0", "#f56c6c"));
        return list;
    }

    private Map<String, List<Number>> buildUserTrend(
            StatsRange r, List<MemberEntity> members, List<DashboardDailyMetric> metrics, List<OrderEntity> orders) {
        List<LocalDate> days = daysInRange(r);
        Map<String, List<Number>> map = new LinkedHashMap<>();
        map.put("new", seriesForDays(days, d -> (int) members.stream()
                .filter(m -> m.getRegisterTime() != null && m.getRegisterTime().toLocalDate().equals(d))
                .count()));
        if (metrics.stream().mapToInt(m -> m.getNewUserCount()).sum() > 0 && map.get("new").stream().mapToInt(Number::intValue).sum() == 0) {
            map.put("new", metrics.stream().map(m -> (Number) m.getNewUserCount()).toList());
        }
        map.put("active", seriesForDays(days, d -> (int) orders.stream()
                .filter(o -> o.getCreateTime() != null && o.getCreateTime().toLocalDate().equals(d))
                .map(this::userKey)
                .distinct()
                .count()));
        map.put("launch", seriesForDays(days, d -> members.stream()
                .filter(m -> m.getRegisterTime() != null && !m.getRegisterTime().toLocalDate().isAfter(d))
                .mapToInt(m -> Optional.ofNullable(m.getLoginCount()).orElse(0))
                .sum()));
        List<Number> total = new ArrayList<>();
        for (LocalDate d : days) {
            int cumulative = (int) members.stream()
                    .filter(m -> m.getRegisterTime() != null && !m.getRegisterTime().toLocalDate().isAfter(d))
                    .count();
            total.add(cumulative);
        }
        map.put("total", total);
        return map;
    }

    private Map<String, List<NameValueVO>> buildMemberRose(
            List<MemberEntity> members, java.util.function.Function<MemberEntity, String> keyFn) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (MemberEntity m : members) {
            String k = keyFn.apply(m);
            counts.merge(k, 1, Integer::sum);
        }
        List<NameValueVO> rose = counts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(8)
                .map(e -> new NameValueVO(labelForKey(e.getKey()), e.getValue()))
                .toList();
        Map<String, List<NameValueVO>> tabs = new LinkedHashMap<>();
        tabs.put("new", rose);
        tabs.put("active", rose);
        tabs.put("total", rose);
        return tabs;
    }

    private Map<String, List<Number>> buildPageTrend(
            StatsRange r, List<OrderEntity> orders, List<DashboardDailyMetric> metrics) {
        List<LocalDate> days = daysInRange(r);
        List<Number> base = seriesForDays(days, d -> (int) orders.stream()
                .filter(o -> o.getCreateTime() != null && o.getCreateTime().toLocalDate().equals(d))
                .count());
        if (base.stream().mapToInt(Number::intValue).sum() == 0 && !metrics.isEmpty()) {
            base = metrics.stream().map(m -> (Number) m.getOrderCount()).toList();
        }
        Map<String, List<Number>> pages = new LinkedHashMap<>();
        pages.put("home", scaleSeries(base, 1.0));
        pages.put("product", scaleSeries(base, 0.85));
        pages.put("banner", scaleSeries(base, 0.6));
        pages.put("activity", scaleSeries(base, 0.45));
        return pages;
    }

    private Map<String, CategoryAgg> aggregateByCategory(List<OrderEntity> orders) {
        Map<String, String> productCategory = productRepository.findAll().stream()
                .filter(p -> !Boolean.TRUE.equals(p.getDeleted()))
                .collect(Collectors.toMap(Product::getProductNo, p -> Optional.ofNullable(p.getCategoryCode()).orElse(""),
                        (a, b) -> a));
        Map<String, String> categoryNames = categoryRepository.findByDeletedFalseOrderBySortNumAsc().stream()
                .collect(Collectors.toMap(ProductCategory::getCode, ProductCategory::getName, (a, b) -> a));

        Map<String, CategoryAgg> agg = new LinkedHashMap<>();
        for (OrderEntity o : orders) {
            if (!isPaid(o)) continue;
            String code = productCategory.getOrDefault(Optional.ofNullable(o.getProductNo()).orElse(""), "");
            String name = categoryNames.getOrDefault(code, code.isEmpty() ? "未分类" : code);
            CategoryAgg a = agg.computeIfAbsent(name, CategoryAgg::new);
            a.qty += Optional.ofNullable(o.getQuantity()).orElse(1);
            a.amount = a.amount.add(o.getActualAmount());
        }
        return agg;
    }

    private List<String> dateLabels(StatsRange r) {
        return daysInRange(r).stream().map(d -> d.format(DAY_LABEL)).toList();
    }

    private List<LocalDate> daysInRange(StatsRange r) {
        List<LocalDate> days = new ArrayList<>();
        for (LocalDate d = r.start(); !d.isAfter(r.end()); d = d.plusDays(1)) {
            days.add(d);
        }
        return days;
    }

    private List<Number> seriesForDays(List<LocalDate> days, java.util.function.Function<LocalDate, Integer> fn) {
        return days.stream().map(d -> (Number) fn.apply(d)).toList();
    }

    private List<Number> scaleSeries(List<Number> base, double factor) {
        return base.stream().map(n -> (int) Math.round(n.doubleValue() * factor)).collect(Collectors.toList());
    }

    private DayOrderStats dayStats(List<OrderEntity> orders, LocalDate day) {
        DayOrderStats s = new DayOrderStats();
        for (OrderEntity o : orders) {
            if (o.getCreateTime() != null && o.getCreateTime().toLocalDate().equals(day)) {
                s.accumulate(o);
            }
        }
        return finalizeDayStats(s);
    }

    private DayOrderStats finalizeDayStats(DayOrderStats s) {
        s.users = s.userSet.size();
        s.payUsers = s.payUserSet.size();
        return s;
    }

    private DayOrderStats mergeMetricWithOrders(List<OrderEntity> allOrders, DashboardDailyMetric metric) {
        DayOrderStats merged = finalizeDayStats(dayStats(allOrders, metric.getStatDate()));
        if (merged.count == 0) {
            merged = DayOrderStats.fromMetric(metric);
        } else if (merged.count < metric.getOrderCount()) {
            merged.count = metric.getOrderCount();
            merged.paidCount = Math.max(merged.paidCount,
                    metric.getOrderCount() - metric.getPendingPaymentCount());
            merged.sales = metric.getSalesAmount();
            merged.validSales = metric.getSalesAmount();
            merged.orderAmount = metric.getSalesAmount();
            merged.validCount = merged.paidCount;
        }
        enrichUsersIfNeeded(merged, allOrders);
        return merged;
    }

    private void enrichUsersIfNeeded(DayOrderStats stats, List<OrderEntity> allOrders) {
        if (allOrders.isEmpty() || stats.count == 0) {
            return;
        }
        if (stats.users == 0) {
            long distinct = allOrders.stream().map(this::userKey).distinct().count();
            stats.users = Math.max(1, (int) Math.round(distinct * (double) stats.count / allOrders.size()));
        }
        if (stats.payUsers == 0) {
            long distinctPay = allOrders.stream().filter(this::isPaid).map(this::userKey).distinct().count();
            stats.payUsers = Math.max(1, (int) Math.round(distinctPay * (double) stats.paidCount / allOrders.size()));
        }
    }

    private StatsKpiVO kpiText(String key, String label, String value, String yesterday, int trend, String bg, String color) {
        StatsKpiVO k = new StatsKpiVO();
        k.setKey(key);
        k.setLabel(label);
        k.setValue(value);
        k.setYesterday(yesterday);
        k.setTrend(trend);
        k.setIconBg(bg);
        k.setIconColor(color);
        return k;
    }

    private boolean isPaid(OrderEntity o) {
        return o.getOrderStatus() != null && !"pending_payment".equals(o.getOrderStatus());
    }

    private String userKey(OrderEntity o) {
        if (o.getUserAccount() != null && !o.getUserAccount().isBlank()) {
            return o.getUserAccount().trim();
        }
        return Optional.ofNullable(o.getReceiverPhone()).orElse(o.getOrderNo());
    }

    private int priceBucket(BigDecimal amount) {
        double v = amount == null ? 0 : amount.doubleValue();
        if (v <= 50) return 0;
        if (v <= 100) return 1;
        if (v <= 200) return 2;
        if (v <= 500) return 3;
        if (v <= 1000) return 4;
        if (v <= 5000) return 5;
        if (v <= 10000) return 6;
        return 7;
    }

    private String mapDeviceSource(String source) {
        if (source == null || source.isBlank()) return "APP";
        String s = source.toUpperCase();
        if (s.contains("微信") || s.contains("MINI")) return "微信小程序";
        if (s.contains("H5")) return "H5";
        if (s.contains("PC")) return "PC站";
        return "APP";
    }

    private String mapChannel(String source) {
        if (source == null || source.isBlank()) return "官方商城";
        return switch (source.toUpperCase()) {
            case "IOS", "APPLE" -> "苹果";
            case "ANDROID", "MI" -> "小米商城";
            case "TENCENT" -> "应用宝";
            default -> source;
        };
    }

    private String labelForKey(String key) {
        return switch (key) {
            case "normal" -> "普通会员";
            case "gold" -> "黄金会员";
            case "platinum" -> "铂金会员";
            case "diamond" -> "钻石会员";
            default -> key;
        };
    }

    private StatsKpiVO kpi(String key, String label, int today, int yesterday, String bg, String color) {
        return kpi(key, label, today, yesterday, bg, color, false);
    }

    private StatsKpiVO kpi(String key, String label, BigDecimal today, BigDecimal yesterday, String bg, String color, boolean money) {
        return kpi(key, label, today, yesterday, bg, color, money, null);
    }

    private StatsKpiVO kpi(String key, String label, int today, int yesterday, String bg, String color, boolean percentSuffix) {
        return kpi(key, label, today, yesterday, bg, color, false, percentSuffix ? "%" : null);
    }

    private StatsKpiVO kpi(String key, String label, Object todayVal, Object yesterdayVal,
                           String bg, String color, boolean money, String suffix) {
        StatsKpiVO k = new StatsKpiVO();
        k.setKey(key);
        k.setLabel(label);
        String suffixStr = suffix == null ? "" : suffix;
        if (todayVal instanceof BigDecimal tb) {
            k.setValue(MONEY_FMT.format(tb) + suffixStr);
            k.setYesterday(yesterdayVal instanceof BigDecimal yb ? MONEY_FMT.format(yb) + suffixStr : String.valueOf(yesterdayVal));
            k.setTrend(trendPercent(tb, yesterdayVal instanceof BigDecimal y ? y : BigDecimal.ZERO));
        } else {
            int t = todayVal instanceof Number n ? n.intValue() : 0;
            int y = yesterdayVal instanceof Number n ? n.intValue() : 0;
            k.setValue(formatCount(t) + suffixStr);
            k.setYesterday(formatCount(y) + suffixStr);
            k.setTrend(trendPercent(t, y));
        }
        k.setIconBg(bg);
        k.setIconColor(color);
        return k;
    }

    private int trendPercent(Number current, Number previous) {
        double c = current.doubleValue();
        double p = previous.doubleValue();
        if (p == 0) return c > 0 ? 100 : 0;
        return (int) Math.round((c - p) / p * 100);
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private String formatCount(int n) {
        return new DecimalFormat("#,###").format(n);
    }

    private String percent(int part, int total) {
        if (total == 0) return "0%";
        return String.format("%.2f%%", part * 100.0 / total);
    }

    private String percent(BigDecimal part, BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) == 0) return "0%";
        return part.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP) + "%";
    }

    private static class DayOrderStats {
        int count;
        int paidCount;
        int refundCount;
        int users;
        int payUsers;
        int validCount;
        BigDecimal sales = BigDecimal.ZERO;
        BigDecimal orderAmount = BigDecimal.ZERO;
        BigDecimal validSales = BigDecimal.ZERO;
        BigDecimal refundAmount = BigDecimal.ZERO;
        final Set<String> userSet = new HashSet<>();
        final Set<String> payUserSet = new HashSet<>();

        void accumulate(OrderEntity o) {
            count++;
            orderAmount = orderAmount.add(o.getActualAmount());
            userSet.add(Optional.ofNullable(o.getReceiverPhone()).orElse(o.getOrderNo()));
            if (isValid(o)) {
                validCount++;
                validSales = validSales.add(o.getActualAmount());
            }
            if (isPaidStatic(o)) {
                paidCount++;
                sales = sales.add(o.getActualAmount());
                payUserSet.add(Optional.ofNullable(o.getUserAccount()).orElse(o.getReceiverPhone()));
            }
            if (o.getAfterSalesStatus() != null && !"none".equals(o.getAfterSalesStatus())) {
                refundCount++;
                refundAmount = refundAmount.add(o.getActualAmount());
            }
        }

        BigDecimal avgPrice() {
            return paidCount == 0 ? BigDecimal.ZERO
                    : sales.divide(BigDecimal.valueOf(paidCount), 2, RoundingMode.HALF_UP);
        }

        static DayOrderStats fromMetric(DashboardDailyMetric m) {
            DayOrderStats s = new DayOrderStats();
            s.count = m.getOrderCount();
            s.paidCount = Math.max(0, m.getOrderCount() - m.getPendingPaymentCount());
            s.sales = m.getSalesAmount();
            s.validSales = m.getSalesAmount();
            s.orderAmount = m.getSalesAmount();
            s.validCount = s.paidCount;
            return s;
        }

        private static boolean isPaidStatic(OrderEntity o) {
            return o.getOrderStatus() != null && !"pending_payment".equals(o.getOrderStatus());
        }

        private static boolean isValid(OrderEntity o) {
            return isPaidStatic(o);
        }
    }

    private static class SourceAgg {
        final String name;
        BigDecimal amount = BigDecimal.ZERO;
        final Set<String> users = new HashSet<>();

        SourceAgg(String name) {
            this.name = name;
        }
    }

    private static class CategoryAgg {
        final String name;
        int qty;
        BigDecimal amount = BigDecimal.ZERO;

        CategoryAgg(String name) {
            this.name = name;
        }
    }

    private static class ProductAgg {
        final String name;
        int qty;
        int pv;
        BigDecimal amount = BigDecimal.ZERO;
        final Set<String> payUsers = new HashSet<>();
        final Set<String> uv = new HashSet<>();

        ProductAgg(String name) {
            this.name = name;
        }
    }
}
