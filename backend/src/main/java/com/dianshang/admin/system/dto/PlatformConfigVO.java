package com.dianshang.admin.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PlatformConfigVO {

    @NotBlank(message = "商城品牌名称不能为空")
    private String shopName;

    @NotBlank(message = "客服热线不能为空")
    private String servicePhone;

    @NotNull(message = "包邮门槛不能为空")
    private BigDecimal freeShipThreshold;

    @NotNull(message = "未付款关单时限不能为空")
    private Integer unpaidCloseMinutes;

    @NotBlank(message = "库存扣减策略不能为空")
    private String stockDeductStrategy;

    private String stockDeductStrategyLabel;
    private String lastUpdateTime;
}
