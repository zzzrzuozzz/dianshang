package com.dianshang.admin.dashboard;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "dashboard_daily_metric")
public class DashboardDailyMetric {

    @Id
    @Column(name = "stat_date")
    private LocalDate statDate;

    @Column(name = "order_count")
    private Integer orderCount;

    @Column(name = "new_user_count")
    private Integer newUserCount;

    @Column(name = "pending_payment_count")
    private Integer pendingPaymentCount;

    @Column(name = "sales_amount")
    private BigDecimal salesAmount;

    @Column(name = "yesterday_sales_amount", nullable = false)
    private BigDecimal yesterdaySalesAmount = BigDecimal.ZERO;
}
