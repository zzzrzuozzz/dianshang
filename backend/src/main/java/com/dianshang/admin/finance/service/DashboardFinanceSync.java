package com.dianshang.admin.finance.service;

import com.dianshang.admin.dashboard.DashboardDailyMetric;
import com.dianshang.admin.dashboard.DashboardDailyMetricRepository;
import com.dianshang.admin.dashboard.DashboardPendingTask;
import com.dianshang.admin.dashboard.DashboardPendingTaskRepository;
import com.dianshang.admin.finance.repository.FinTransactionRecordRepository;
import com.dianshang.admin.finance.repository.FinWithdrawApplyRepository;
import com.dianshang.admin.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 将订单/财务真实数据同步到首页看板，保证各模块指标一致。
 */
@Service
public class DashboardFinanceSync {

    private final DashboardDailyMetricRepository metricRepository;
    private final DashboardPendingTaskRepository pendingTaskRepository;
    private final FinTransactionRecordRepository transactionRepository;
    private final FinWithdrawApplyRepository withdrawRepository;
    private final OrderRepository orderRepository;

    public DashboardFinanceSync(DashboardDailyMetricRepository metricRepository,
                                DashboardPendingTaskRepository pendingTaskRepository,
                                FinTransactionRecordRepository transactionRepository,
                                FinWithdrawApplyRepository withdrawRepository,
                                OrderRepository orderRepository) {
        this.metricRepository = metricRepository;
        this.pendingTaskRepository = pendingTaskRepository;
        this.transactionRepository = transactionRepository;
        this.withdrawRepository = withdrawRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public void refreshGlobalMetrics() {
        refreshPendingTasks();
        refreshRecentDailyMetrics(7);
    }

    @Transactional
    public void refreshPendingTasks() {
        updateTask("pendingShipment", "待发货订单",
                orderRepository.countByDeletedFalseAndOrderStatus("pending_ship"));
        updateTask("pendingRefund", "待处理退款",
                orderRepository.countByDeletedFalseAndOrderStatus("refunded"));
        updateTask("pendingReceipt", "待确认收货",
                orderRepository.countByDeletedFalseAndOrderStatusAndShipStatusNot("shipped", "signed"));
        updateTask("pendingAfterSale", "待处理售后",
                orderRepository.countByDeletedFalseAndAfterSalesStatusNot("none"));
        updateTask("pendingWithdraw", "待审核提现",
                withdrawRepository.countByVerifyStatus(0));
    }

    @Transactional
    public void refreshRecentDailyMetrics(int days) {
        LocalDate end = LocalDate.now();
        BigDecimal prevSales = salesForDay(end.minusDays(days));
        for (int i = days - 1; i >= 0; i--) {
            LocalDate day = end.minusDays(i);
            LocalDateTime start = day.atStartOfDay();
            LocalDateTime endEx = day.plusDays(1).atStartOfDay();
            long orders = orderRepository.countByDeletedFalseAndCreateTimeBetween(start, endEx);
            BigDecimal sales = salesForDay(day);
            DashboardDailyMetric metric = metricRepository.findById(day).orElse(new DashboardDailyMetric());
            metric.setStatDate(day);
            metric.setOrderCount((int) orders);
            metric.setSalesAmount(sales);
            metric.setYesterdaySalesAmount(prevSales);
            metric.setPendingPaymentCount((int) orderRepository.findByDeletedFalseAndCreateTimeBetween(start, endEx).stream()
                    .filter(o -> "pending_payment".equals(o.getOrderStatus())).count());
            if (metric.getNewUserCount() == null) {
                metric.setNewUserCount(0);
            }
            metricRepository.save(metric);
            prevSales = sales;
        }
    }

    private BigDecimal salesForDay(LocalDate day) {
        LocalDateTime start = day.atStartOfDay();
        LocalDateTime endEx = day.plusDays(1).atStartOfDay();
        long orders = orderRepository.countByDeletedFalseAndCreateTimeBetween(start, endEx);
        BigDecimal sales = nz(transactionRepository.sumOrderInBetween(start, endEx));
        if (sales.compareTo(BigDecimal.ZERO) == 0 && orders > 0) {
            sales = orderRepository.findByDeletedFalseAndCreateTimeBetween(start, endEx).stream()
                    .filter(o -> !"pending_payment".equals(o.getOrderStatus()))
                    .map(o -> o.getActualAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        return sales;
    }

    private void updateTask(String key, String label, long count) {
        DashboardPendingTask task = pendingTaskRepository.findById(key).orElseGet(() -> {
            DashboardPendingTask t = new DashboardPendingTask();
            t.setTaskKey(key);
            t.setLabel(label);
            t.setSortNum(99);
            return t;
        });
        task.setLabel(label);
        task.setCountValue((int) Math.min(count, Integer.MAX_VALUE));
        pendingTaskRepository.save(task);
    }

    private BigDecimal nz(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}
