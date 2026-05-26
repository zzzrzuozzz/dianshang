package com.dianshang.admin.content.help.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class HelpArticleSaveRequest {

    private String articleCode;

    @NotNull(message = "请选择帮助类型")
    private Long typeId;

    @NotBlank(message = "请输入帮助标题")
    private String title;

    private String intro;
    private String content;
    private List<String> coverImages;
    private Boolean online;
    private Integer sort;
}
