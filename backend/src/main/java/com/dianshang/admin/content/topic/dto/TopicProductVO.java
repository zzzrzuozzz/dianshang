package com.dianshang.admin.content.topic.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TopicProductVO {

    private String id;
    private String name;
    private BigDecimal price;
    private String thumb;
}
