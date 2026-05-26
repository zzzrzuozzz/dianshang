package com.dianshang.admin.permission.dto;

import lombok.Data;

@Data
public class InviteCodeVO {

    private Long id;
    private String code;
    private Long roleId;
    private String roleName;
    private Integer used;
    private String usedLabel;
    private Long usedByAdminId;
    private String usedByUsername;
    private String usedAccount;
    private String usedAccountType;
    private String usedTime;
    private String createTime;
    private String remark;
}
