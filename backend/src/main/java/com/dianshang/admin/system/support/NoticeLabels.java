package com.dianshang.admin.system.support;

import java.util.Map;

public final class NoticeLabels {

    public static final Map<String, String> TYPE = Map.of(
            "SYSTEM", "系统公告",
            "AUDIT", "审批待办",
            "ALARM", "风险预警"
    );

    public static final Map<String, String> LEVEL = Map.of(
            "INFO", "普通",
            "WARNING", "重要",
            "DANGER", "紧急"
    );

    private NoticeLabels() {
    }
}
