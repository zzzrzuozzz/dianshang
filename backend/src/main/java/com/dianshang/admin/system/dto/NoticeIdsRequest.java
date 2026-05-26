package com.dianshang.admin.system.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class NoticeIdsRequest {

    @NotEmpty(message = "ids 不能为空")
    private List<Long> ids;
}
