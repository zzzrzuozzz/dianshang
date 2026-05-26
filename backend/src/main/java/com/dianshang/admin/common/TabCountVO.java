package com.dianshang.admin.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TabCountVO {

    private String key;
    private String label;
    private long count;
}
