package com.dianshang.admin.stats.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatsKpiVO {
    private String key;
    private String label;
    private String value;
    private String yesterday;
    private int trend;
    private String iconBg;
    private String iconColor;
}
