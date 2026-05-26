package com.dianshang.admin.product.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BatchAuditRequest {

    @NotEmpty
    private List<String> ids;
    private Boolean passed;
}
