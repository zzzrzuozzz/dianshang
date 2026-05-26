package com.dianshang.admin.dashboard;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DashboardPendingTaskRepository extends JpaRepository<DashboardPendingTask, String> {

    List<DashboardPendingTask> findAllByOrderBySortNumAsc();
}
