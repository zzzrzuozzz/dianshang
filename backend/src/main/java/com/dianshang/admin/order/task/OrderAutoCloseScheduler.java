package com.dianshang.admin.order.task;

import com.dianshang.admin.order.entity.OrderEntity;
import com.dianshang.admin.order.repository.OrderRepository;
import com.dianshang.admin.system.service.SysConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 按 sys_config.unpaid_close_minutes 自动关闭超时未付款订单。
 */
@Component
public class OrderAutoCloseScheduler {

    private static final Logger log = LoggerFactory.getLogger(OrderAutoCloseScheduler.class);

    private final OrderRepository orderRepository;
    private final SysConfigService sysConfigService;

    public OrderAutoCloseScheduler(OrderRepository orderRepository, SysConfigService sysConfigService) {
        this.orderRepository = orderRepository;
        this.sysConfigService = sysConfigService;
    }

    @Scheduled(fixedDelay = 60_000, initialDelay = 30_000)
    @Transactional
    public void closeExpiredUnpaidOrders() {
        int minutes = sysConfigService.getUnpaidCloseMinutes();
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(minutes);
        List<OrderEntity> expired = orderRepository.findByDeletedFalseAndOrderStatusAndCreateTimeBefore(
                "pending_payment", deadline);
        if (expired.isEmpty()) {
            return;
        }
        for (OrderEntity order : expired) {
            order.setOrderStatus("closed");
            order.setShipStatus("none");
        }
        orderRepository.saveAll(expired);
        log.info("自动关闭未付款订单 {} 笔（超时 {} 分钟）", expired.size(), minutes);
    }
}
