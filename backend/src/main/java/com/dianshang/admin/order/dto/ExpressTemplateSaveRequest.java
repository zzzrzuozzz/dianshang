package com.dianshang.admin.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ExpressTemplateSaveRequest {

    @NotBlank(message = "请输入模板名称")
    @Size(max = 100)
    private String templateName;

    @NotBlank(message = "请选择快递公司")
    @Size(max = 50)
    private String expressCompany;

    @Size(max = 50)
    private String templateSpec;

    @Size(max = 200)
    private String remark;

    private Boolean isDefault;
    private Boolean visible;
    private Integer sort;
}
