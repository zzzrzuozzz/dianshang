package com.dianshang.admin.system.dto;

import lombok.Data;

import java.util.List;

@Data
public class MaintenanceStatusVO {

    private List<InitializerLine> initializers;
    private String cacheClearedAt;
    private long serverTimeMs;

    @Data
    public static class InitializerLine {
        private String name;
        private String status;
        private String detail;
    }
}
