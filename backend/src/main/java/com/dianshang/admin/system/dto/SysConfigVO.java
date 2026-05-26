package com.dianshang.admin.system.dto;

import lombok.Data;

@Data
public class SysConfigVO {

    private Long id;
    private String configKey;
    private String configValue;
    private String remark;
    private String updateTime;
}
