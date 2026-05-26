package com.dianshang.admin.config;

import com.dianshang.admin.dashboard.DashboardDailyMetric;
import com.dianshang.admin.dashboard.DashboardDailyMetricRepository;
import com.dianshang.admin.order.entity.OrderEntity;
import com.dianshang.admin.order.repository.OrderRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 若订单创建时间均不在近 30 天内，则按看板指标日期分布对齐，便于统计按日聚合。
 */
@Component
public class StatsOrderAlignInitializer implements CommandLineRunner {

    private final OrderRepository orderRepository;
    private final DashboardDailyMetricRepository metricRepository;

    public StatsOrderAlignInitializer(OrderRepository orderRepository,
                                      DashboardDailyMetricRepository metricRepository) {
        this.orderRepository = orderRepository;
        this.metricRepository = metricRepository;
    }

    @Override
    public void run(String... args) {
        List<OrderEntity> orders = orderRepository.findByDeletedFalse();
        if (orders.isEmpty()) {
            return;
        }
        LocalDate cutoff = LocalDate.now().minusDays(30);
        boolean hasRecent = orders.stream().anyMatch(o ->
                o.getCreateTime() != null && !o.getCreateTime().toLocalDate().isBefore(cutoff));
        if (hasRecent) {
            return;
        }
        List<LocalDate> days = metricRepository.findAllByOrderByStatDateAsc().stream()
                .map(DashboardDailyMetric::getStatDate)
                .toList();
        if (days.isEmpty()) {
            return;
        }
        for (int i = 0; i < orders.size(); i++) {
            OrderEntity o = orders.get(i);
            LocalDate day = days.get(i % days.size());
            int hour = 9 + (i % 10);
            int minute = (i * 11) % 60;
            o.setCreateTime(day.atTime(hour, minute));
            if (o.getPayTime() == null && isPaid(o)) {
                o.setPayTime(o.getCreateTime().plusHours(1));
            }
            orderRepository.save(o);
        }
    }

    private static boolean isPaid(OrderEntity o) {
        return o.getOrderStatus() != null && !"pending_payment".equals(o.getOrderStatus());
    }
}
