package com.dianshang.admin.common;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TreeNodeVO {

    private Long id;
    private String label;
    private List<TreeNodeVO> children = new ArrayList<>();
}
