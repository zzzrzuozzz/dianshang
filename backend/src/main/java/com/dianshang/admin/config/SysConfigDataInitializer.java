package com.dianshang.admin.config;

import com.dianshang.admin.system.service.SysConfigService;
import com.dianshang.admin.system.support.SysConfigKeys;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(5)
public class SysConfigDataInitializer implements CommandLineRunner {

    private final SysConfigService sysConfigService;

    public SysConfigDataInitializer(SysConfigService sysConfigService) {
        this.sysConfigService = sysConfigService;
    }

    @Override
    public void run(String... args) {
        sysConfigService.ensureDefault(SysConfigKeys.SHOP_NAME, "暴走电商", "商城品牌名称");
        sysConfigService.ensureDefault(SysConfigKeys.SERVICE_PHONE, "400-888-8888", "官方客服热线");
        sysConfigService.ensureDefault(SysConfigKeys.FREE_SHIP_THRESHOLD, "99", "满额包邮门槛(元)");
        sysConfigService.ensureDefault(SysConfigKeys.UNPAID_CLOSE_MINUTES, "30", "未付款订单自动关闭(分钟)");
        sysConfigService.ensureDefault(SysConfigKeys.STOCK_DEDUCT_STRATEGY, "order", "库存扣减策略: order|pay");
    }
}
