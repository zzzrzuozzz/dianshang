package com.dianshang.admin.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class MemberUpdateRequest {

    @NotBlank
    private String userId;
    private String phone;
    private String level;
    private String avatar;
    private String gender;
    private List<String> city;
    private String password;
    private List<String> permissions;
    private String remark;
}
