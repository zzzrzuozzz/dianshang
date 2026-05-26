package com.dianshang.admin.order.service;

import com.dianshang.admin.common.BusinessException;
import com.dianshang.admin.common.TabCountVO;
import com.dianshang.admin.finance.service.FinanceOrderBridge;
import com.dianshang.admin.inventory.service.InventoryService;
import com.dianshang.admin.order.dto.AfterSaleListVO;
import com.dianshang.admin.order.dto.AfterSalePageVO;
import com.dianshang.admin.order.entity.AfterSaleEntity;
import com.dianshang.admin.order.entity.OrderEntity;
import com.dianshang.admin.order.repository.AfterSaleRepository;
import com.dianshang.admin.order.repository.OrderRepository;
import com.dianshang.admin.order.support.OrderSpecifications;
import com.dianshang.admin.permission.support.PermissionSecurityHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AfterSaleService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-M-d HH:mm");

    private final AfterSaleRepository afterSaleRepository;
    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    private final FinanceOrderBridge financeOrderBridge;
    private final PermissionSecurityHelper permissionSecurityHelper;

    public AfterSaleService(AfterSaleRepository afterSaleRepository,
                            OrderRepository orderRepository,
                            InventoryService inventoryService,
                            FinanceOrderBridge financeOrderBridge,
                            PermissionSecurityHelper permissionSecurityHelper) {
        this.afterSaleRepository = afterSaleRepository;
        this.orderRepository = orderRepository;
        this.inventoryService = inventoryService;
        this.financeOrderBridge = financeOrderBridge;
        this.permissionSecurityHelper = permissionSecurityHelper;
    }

    public AfterSalePageVO list(String product, String orderId, String logisticsNo, String afterSaleId,
                                String timeType, LocalDate start, LocalDate end,
                                String status, int page, int pageSize) {
        Page<AfterSaleEntity> result = afterSaleRepository.findAll(
                OrderSpecifications.forAfterSale(product, orderId, logisticsNo, afterSaleId, timeType, start, end, status),
                PageRequest.of(Math.max(page - 1, 0), pageSize, Sort.by(Sort.Direction.DESC, "applyTime")));

        AfterSalePageVO vo = new AfterSalePageVO();
        vo.setList(result.getContent().stream().map(this::toVO).toList());
        vo.setTotal(result.getTotalElements());
        vo.setPage(page);
        vo.setPageSize(pageSize);
        vo.setTotalPages((int) Math.ceil((double) result.getTotalElements() / pageSize));
        vo.setTabs(buildTabs());
        return vo;
    }

    @Transactional
    public void approve(String afterSaleNo) {
        permissionSecurityHelper.assertPerm("order:refund");
        AfterSaleEntity afterSale = requireAfterSale(afterSaleNo);
        if (!"platform_pending".equals(afterSale.getAfterSaleStatus())) {
            throw new BusinessException("当前售后状态不可同意");
        }
        OrderEntity order = requireOrder(afterSale.getOrderNo());
        LocalDateTime now = LocalDateTime.now();
        afterSale.setProcessTime(now);

        if ("refund_only".equals(afterSale.getAfterSaleType())) {
            finishRefund(order, afterSale);
            return;
        }
        afterSale.setAfterSaleStatus("user_pending");
        afterSale.setOrderStatus(order.getOrderStatus());
        afterSale.setShipStatus(order.getShipStatus());
        order.setAfterSalesStatus("processing");
        afterSaleRepository.save(afterSale);
        orderRepository.save(order);
    }

    @Transactional
    public void reject(String afterSaleNo, String remark) {
        permissionSecurityHelper.assertPerm("order:refund");
        AfterSaleEntity afterSale = requireAfterSale(afterSaleNo);
        if (!"platform_pending".equals(afterSale.getAfterSaleStatus())) {
            throw new BusinessException("当前售后状态不可拒绝");
        }
        OrderEntity order = requireOrder(afterSale.getOrderNo());
        afterSale.setAfterSaleStatus("rejected");
        afterSale.setProcessTime(LocalDateTime.now());
        order.setAfterSalesStatus("rejected");
        afterSaleRepository.save(afterSale);
        orderRepository.save(order);
    }

    @Transactional
    public void confirmReturn(String afterSaleNo) {
        permissionSecurityHelper.assertPerm("order:refund");
        AfterSaleEntity afterSale = requireAfterSale(afterSaleNo);
        if (!"platform_confirm".equals(afterSale.getAfterSaleStatus())) {
            throw new BusinessException("当前售后状态不可确认收货");
        }
        OrderEntity order = requireOrder(afterSale.getOrderNo());
        finishRefund(order, afterSale);
    }

    private void finishRefund(OrderEntity order, AfterSaleEntity afterSale) {
        order.setOrderStatus("refunded");
        order.setAfterSalesStatus("refunded");
        afterSale.setAfterSaleStatus("completed");
        afterSale.setOrderStatus("refunded");
        afterSale.setProcessTime(LocalDateTime.now());
        orderRepository.save(order);
        afterSaleRepository.save(afterSale);
        int qty = order.getQuantity() != null ? order.getQuantity() : 1;
        inventoryService.returnInbound(order, qty);
        financeOrderBridge.recordRefund(order);
    }

    private AfterSaleEntity requireAfterSale(String afterSaleNo) {
        return afterSaleRepository.findByAfterSaleNoAndDeletedFalse(afterSaleNo)
                .orElseThrow(() -> new BusinessException("售后单不存在"));
    }

    private OrderEntity requireOrder(String orderNo) {
        return orderRepository.findByOrderNoAndDeletedFalse(orderNo)
                .orElseThrow(() -> new BusinessException("订单不存在"));
    }

    private List<TabCountVO> buildTabs() {
        return List.of(
                new TabCountVO("platform_pending", "待平台处理", afterSaleRepository.countByDeletedFalseAndAfterSaleStatus("platform_pending")),
                new TabCountVO("user_pending", "待用户处理", afterSaleRepository.countByDeletedFalseAndAfterSaleStatus("user_pending")),
                new TabCountVO("platform_confirm", "待平台确认收货", afterSaleRepository.countByDeletedFalseAndAfterSaleStatus("platform_confirm")),
                new TabCountVO("completed", "已完成", afterSaleRepository.countByDeletedFalseAndAfterSaleStatus("completed")),
                new TabCountVO("rejected", "已拒绝", afterSaleRepository.countByDeletedFalseAndAfterSaleStatus("rejected")),
                new TabCountVO("closed", "已关闭", afterSaleRepository.countByDeletedFalseAndAfterSaleStatus("closed")),
                new TabCountVO("all", "全部", afterSaleRepository.countByDeletedFalse())
        );
    }

    private AfterSaleListVO toVO(AfterSaleEntity a) {
        AfterSaleListVO vo = new AfterSaleListVO();
        vo.setId(a.getAfterSaleNo());
        vo.setOrderId(a.getOrderNo());
        vo.setProductName(a.getProductName());
        vo.setThumb(a.getThumb());
        vo.setOrderStatus(a.getOrderStatus());
        vo.setShipStatus(a.getShipStatus());
        vo.setAfterSaleStatus(a.getAfterSaleStatus());
        vo.setAfterSaleType(a.getAfterSaleType());
        vo.setRefundAmount(a.getRefundAmount());
        vo.setApplyTime(format(a.getApplyTime()));
        vo.setProcessTime(format(a.getProcessTime()));
        return vo;
    }

    private String format(java.time.LocalDateTime time) {
        return time != null ? time.format(FMT) : "";
    }
}
