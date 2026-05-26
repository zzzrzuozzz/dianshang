package com.dianshang.admin.content.help.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class HelpTypeSaveRequest {

    private Long id;

    @NotBlank(message = "请输入分类名称")
    private String name;

    private Integer sort;

    private Boolean visible;

    private String icon;
}
