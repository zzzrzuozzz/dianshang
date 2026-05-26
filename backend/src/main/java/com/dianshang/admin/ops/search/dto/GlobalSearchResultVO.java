package com.dianshang.admin.ops.search.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class GlobalSearchResultVO {
    private List<GlobalSearchItemVO> products = new ArrayList<>();
    private List<GlobalSearchItemVO> orders = new ArrayList<>();
    private List<GlobalSearchItemVO> users = new ArrayList<>();
}
