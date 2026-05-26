package com.dianshang.admin.promotion.dto;

import lombok.Data;

@Data
public class PromoActivityVO {

    private String id;
    private String title;
    private String status;
    private String startTime;
    private String endTime;
    private String warning;
    private Boolean online;
}
