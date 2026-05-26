package com.dianshang.admin.dashboard;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DashboardDailyMetricRepository extends JpaRepository<DashboardDailyMetric, LocalDate> {

    List<DashboardDailyMetric> findAllByOrderByStatDateAsc();

    List<DashboardDailyMetric> findByStatDateBetweenOrderByStatDateAsc(LocalDate start, LocalDate end);
}
