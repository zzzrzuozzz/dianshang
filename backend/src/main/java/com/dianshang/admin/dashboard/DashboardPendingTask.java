package com.dianshang.admin.dashboard;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "dashboard_pending_task")
public class DashboardPendingTask {

    @Id
    @Column(name = "task_key")
    private String taskKey;

    private String label;

    @Column(name = "count_value")
    private Integer countValue;

    @Column(name = "sort_num")
    private Integer sortNum;
}
