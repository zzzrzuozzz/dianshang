package com.dianshang.admin.stats.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class FlowReportVO {
    private List<StatsKpiVO> kpis = new ArrayList<>();
    private List<String> dates = new ArrayList<>();
    private Map<String, List<Number>> userTrend = new LinkedHashMap<>();
    private Map<String, List<NameValueVO>> versionRose = new LinkedHashMap<>();
    private Map<String, List<Number>> pageTrend = new LinkedHashMap<>();
    private Map<String, List<NameValueVO>> channelRose = new LinkedHashMap<>();
}
