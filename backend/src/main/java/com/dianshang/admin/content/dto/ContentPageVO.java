package com.dianshang.admin.content.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ContentPageVO<T> {

    private List<T> list;
    private long total;
    private int page;
    private int pageSize;
    private int totalPages;
    private Map<String, Integer> counts;
}
