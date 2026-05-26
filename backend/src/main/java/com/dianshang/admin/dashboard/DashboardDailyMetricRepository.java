package com.dianshang.admin.dashboard;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DashboardDailyMetricRepository extends JpaRepository<DashboardDailyMetric, java.time.LocalDate> {

    List<DashboardDailyMetric> findAllByOrderByStatDateAsc();
}
