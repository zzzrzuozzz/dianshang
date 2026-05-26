package com.dianshang.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.upload")
public class UploadProperties {

    /** 上传根目录（相对 backend 工作目录） */
    private String dir = "./uploads";

    private long maxSizeMb = 5;
}
