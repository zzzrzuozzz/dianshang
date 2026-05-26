package com.dianshang.admin.permission.dto;

import lombok.Data;

@Data
public class InviteCodePageRequest {

    /** null=全部, 0=未使用, 1=已使用 */
    private Integer used;
    private int page = 1;
    private int pageSize = 10;
}
