package com.dianshang.admin.order.service;

import com.dianshang.admin.common.BusinessException;
import com.dianshang.admin.common.TabCountVO;
import com.dianshang.admin.order.dto.*;
import com.dianshang.admin.order.entity.AfterSaleEntity;
import com.dianshang.admin.order.entity.OrderEntity;
import com.dianshang.admin.order.repository.AfterSaleRepository;
import com.dianshang.admin.order.repository.OrderRepository;
import com.dianshang.admin.finance.service.FinanceOrderBridge;
import com.dianshang.admin.inventory.service.InventoryService;
import com.dianshang.admin.permission.support.PermissionSecurityHelper;
import com.dianshang.admin.system.service.SysConfigService;
import com.dianshang.admin.order.support.OrderDetailAssembler;
import com.dianshang.admin.order.support.OrderListMapper;
import com.dianshang.admin.order.support.OrderSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final AfterSaleRepository afterSaleRepository;
    private final OrderAddressService orderAddressService;
    private final ExpressTemplateService expressTemplateService;
    private final InventoryService inventoryService;
    private final FinanceOrderBridge financeOrderBridge;
    private final SysConfigService sysConfigService;
    private final PermissionSecurityHelper permissionSecurityHelper;

    public OrderService(OrderRepository orderRepository,
                        AfterSaleRepository afterSaleRepository,
                        OrderAddressService orderAddressService,
                        ExpressTemplateService expressTemplateService,
                        InventoryService inventoryService,
                        FinanceOrderBridge financeOrderBridge,
                        SysConfigService sysConfigService,
                        PermissionSecurityHelper permissionSecurityHelper) {
        this.orderRepository = orderRepository;
        this.afterSaleRepository = afterSaleRepository;
        this.orderAddressService = orderAddressService;
        this.expressTemplateService = expressTemplateService;
        this.inventoryService = inventoryService;
        this.financeOrderBridge = financeOrderBridge;
        this.sysConfigService = sysConfigService;
        this.permissionSecurityHelper = permissionSecurityHelper;
    }

    public OrderPageVO list(String product, String orderId, String logisticsNo, String phone,
                            String timeType, LocalDate start, LocalDate end,
                            String status, String pageType, int page, int pageSize) {
        Page<OrderEntity> result = orderRepository.findAll(
                OrderSpecifications.forList(product, orderId, logisticsNo, phone, timeType, start, end, status, pageType),
                PageRequest.of(Math.max(page - 1, 0), pageSize, Sort.by(Sort.Direction.DESC, "createTime")));

        OrderPageVO vo = new OrderPageVO();
        vo.setList(result.getContent().stream().map(OrderListMapper::toVO).toList());
        vo.setTotal(result.getTotalElements());
        vo.setPage(page);
        vo.setPageSize(pageSize);
        vo.setTotalPages((int) Math.ceil((double) result.getTotalElements() / pageSize));
        vo.setActiveTab(StringUtils.hasText(status) ? status : defaultTab(pageType));
        vo.setTabs(buildOrderTabs(pageType));
        return vo;
    }

    private String defaultTab(String pageType) {
        return "confirm".equals(pageType) ? "shipped" : "pending_ship";
    }

    private List<TabCountVO> buildOrderTabs(String pageType) {
        if ("confirm".equals(pageType)) {
            long confirmCount = orderRepository.countByDeletedFalseAndOrderStatusAndShipStatusNot("shipped", "signed");
            return List.of(new TabCountVO("shipped", "待确认收货", confirmCount));
        }
        return List.of(
                tab("all", "全部"),
                tab("pending_payment", "待付款"),
                tab("pending_ship", "待发货"),
                tab("shipped", "已发货"),
                tab("after_sale", "售后订单"),
                tab("completed", "已完成"),
                tab("closed", "已关闭"),
                tab("refunded", "已退款")
        );
    }

    private TabCountVO tab(String key, String label) {
        long count;
        if ("all".equals(key)) {
            count = orderRepository.countByDeletedFalse();
        } else if ("after_sale".equals(key)) {
            count = orderRepository.countByDeletedFalseAndAfterSalesStatusNot("none");
        } else {
            count = orderRepository.countByDeletedFalseAndOrderStatus(key);
        }
        return new TabCountVO(key, label, count);
    }

    public OrderDetailVO detail(String orderNo) {
        OrderEntity order = requireOrder(orderNo);
        AfterSaleEntity afterSale = afterSaleRepository.findAll(
                (root, q, cb) -> cb.and(
                        cb.isFalse(root.get("deleted")),
                        cb.equal(root.get("orderNo"), orderNo)
                )).stream()
                .filter(a -> !"closed".equals(a.getAfterSaleStatus()))
                .max(Comparator.comparing(AfterSaleEntity::getApplyTime,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);
        String returnAddress = orderAddressService.resolveDefaultFullAddress("return");
        if (returnAddress == null) {
            returnAddress = "广东省深圳市南山区科技园退货中心A栋";
        }
        OrderDetailVO vo = OrderDetailAssembler.assemble(order, afterSale, returnAddress);
        applyFreightFreeHint(order, vo);
        vo.setPlatformHints(sysConfigService.buildPlatformHints());
        return vo;
    }

    private void applyFreightFreeHint(OrderEntity order, OrderDetailVO vo) {
        if (vo.getPayment() == null) {
            return;
        }
        BigDecimal threshold = sysConfigService.getFreeShipThreshold();
        boolean eligible = order.getActualAmount() != null
                && order.getActualAmount().compareTo(threshold) >= 0;
        if (eligible && Boolean.TRUE.equals(order.getFreightFree())) {
            vo.getPayment().setFreightNote("已满足平台包邮规则（满 " + threshold.stripTrailingZeros().toPlainString() + " 元）");
        } else if (eligible) {
            vo.getPayment().setFreightNote("订单金额已达包邮门槛 " + threshold.stripTrailingZeros().toPlainString() + " 元");
        }
    }

    @Transactional
    public void ship(String orderNo, ShipRequest request) {
        permissionSecurityHelper.assertPerm("order:ship");
        OrderEntity order = requireOrder(orderNo);
        if (!"pending_ship".equals(order.getOrderStatus()) && !"paid".equals(order.getOrderStatus())) {
            throw new BusinessException("当前订单状态不可发货");
        }
        order.setOrderStatus("shipped");
        order.setShipStatus("in_transit");
        if (request != null && StringUtils.hasText(request.getLogistics())) {
            order.setLogistics(request.getLogistics());
            order.setLogisticsNo(request.getLogisticsNo());
        } else {
            var template = expressTemplateService.findDefault();
            if (template != null) {
                order.setLogistics(template.getExpressCompany() + " (ST" + orderNo + ")");
            } else {
                order.setLogistics("申通快递 (ST" + orderNo + ")");
            }
            order.setLogisticsNo("ST" + orderNo);
        }
        order.setDeliverySerial("FH" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + orderNo);
        orderRepository.save(order);
        inventoryService.deductStockForOrder(order, "ship");
        financeOrderBridge.recordIncomeIfPaid(order);
    }

    @Transactional
    public void refund(String orderNo) {
        permissionSecurityHelper.assertPerm("order:refund");
        OrderEntity order = requireOrder(orderNo);
        order.setOrderStatus("refunded");
        order.setAfterSalesStatus("refunded");
        orderRepository.save(order);

        AfterSaleEntity afterSale = new AfterSaleEntity();
        afterSale.setAfterSaleNo("AS" + System.currentTimeMillis() % 100000);
        afterSale.setOrderNo(orderNo);
        afterSale.setProductName(order.getProductName());
        afterSale.setThumb(order.getThumb());
        afterSale.setOrderStatus(order.getOrderStatus());
        afterSale.setShipStatus(order.getShipStatus());
        afterSale.setAfterSaleStatus("platform_pending");
        afterSale.setAfterSaleType("refund_only");
        afterSale.setRefundAmount(order.getActualAmount());
        afterSale.setApplyTime(LocalDateTime.now());
        afterSaleRepository.save(afterSale);
        int qty = order.getQuantity() != null ? order.getQuantity() : 1;
        inventoryService.returnInbound(order, qty);
        financeOrderBridge.recordRefund(order);
    }

    @Transactional
    public void reissue(String orderNo, ShipRequest request) {
        OrderEntity order = requireOrder(orderNo);
        if (!"shipped".equals(order.getOrderStatus()) && !"completed".equals(order.getOrderStatus())) {
            throw new BusinessException("仅已发货或已完成订单可补发");
        }
        if (request != null && StringUtils.hasText(request.getLogistics())) {
            order.setLogistics(request.getLogistics());
            order.setLogisticsNo(request.getLogisticsNo());
        } else {
            var template = expressTemplateService.findDefault();
            String company = template != null ? template.getExpressCompany() : "申通快递";
            order.setLogistics(company + " (补发-" + orderNo + ")");
            order.setLogisticsNo("ST-R" + orderNo);
        }
        if ("completed".equals(order.getOrderStatus())) {
            order.setShipStatus("in_transit");
        }
        order.setDeliverySerial("FH-R" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + orderNo);
        orderRepository.save(order);
        inventoryService.reissueOutbound(order);
    }

    @Transactional
    public void confirm(String orderNo) {
        OrderEntity order = requireOrder(orderNo);
        if (!"shipped".equals(order.getOrderStatus())) {
            throw new BusinessException("仅已发货订单可确认收货");
        }
        order.setOrderStatus("completed");
        order.setShipStatus("signed");
        order.setAutoConfirmTime(LocalDateTime.now());
        orderRepository.save(order);
        financeOrderBridge.recordIncomeIfPaid(order);
    }

    @Transactional
    public void batchConfirm(List<String> orderNos) {
        orderNos.forEach(this::confirm);
    }

    private OrderEntity requireOrder(String orderNo) {
        return orderRepository.findByOrderNoAndDeletedFalse(orderNo)
                .orElseThrow(() -> new BusinessException("订单不存在"));
    }
}
