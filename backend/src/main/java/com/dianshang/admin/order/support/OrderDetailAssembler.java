package com.dianshang.admin.order.support;

import com.dianshang.admin.order.dto.OrderDetailVO;
import com.dianshang.admin.order.entity.AfterSaleEntity;
import com.dianshang.admin.order.entity.OrderEntity;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class OrderDetailAssembler {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-M-d HH:mm");

    private static final Map<String, String> STATUS_TEXT = Map.of(
            "pending_payment", "待付款",
            "pending_ship", "待发货",
            "paid", "已付款",
            "shipped", "已发货",
            "completed", "已完成",
            "closed", "已关闭",
            "refunded", "已退款",
            "after_sale", "售后中"
    );

    private static final Map<String, String> AFTER_SALE_TYPE_TEXT = Map.of(
            "refund_only", "仅退款",
            "return_refund", "退货退款",
            "exchange", "换货"
    );

    private static final Map<String, String> AFTER_SALE_STATUS_TEXT = Map.of(
            "platform_pending", "待平台处理",
            "user_pending", "待用户处理",
            "platform_confirm", "待平台确认收货",
            "completed", "已完成",
            "rejected", "已拒绝",
            "closed", "已关闭"
    );

    private OrderDetailAssembler() {
    }

    public static OrderDetailVO assemble(OrderEntity order, AfterSaleEntity afterSale, String defaultReturnAddress) {
        OrderDetailVO vo = new OrderDetailVO();
        vo.setId(order.getOrderNo());
        vo.setOrderStatus(order.getOrderStatus());
        vo.setOrderStatusText(STATUS_TEXT.getOrDefault(order.getOrderStatus(), order.getOrderStatus()));
        vo.setShipStatus(order.getShipStatus());
        vo.setShipStatusText(buildShipStatusText(order));
        vo.setAfterSalesStatus(order.getAfterSalesStatus());
        vo.setAfterSalesStatusText(afterSalesStatusText(order.getAfterSalesStatus()));
        vo.setDeliverySerial(nullToDash(order.getDeliverySerial()));
        vo.setUserAccount(order.getUserAccount());
        vo.setPayMethod(nullToDash(order.getPayMethod()));
        vo.setPayMethodDetail(order.getPayMethodDetail());
        vo.setOrderSource(order.getOrderSource());
        vo.setOrderType(order.getOrderType());
        vo.setDeliveryMethod(order.getDeliveryMethod());
        vo.setLogisticsNo(nullToDash(order.getLogisticsNo()));
        vo.setAutoConfirmDays(order.getAutoConfirmDaysLabel());
        vo.setReceiverName(order.getReceiverName());
        vo.setReceiverPhone(order.getReceiverPhone());
        vo.setReceiverAddress(order.getReceiverAddress());
        vo.setUserRemark(order.getUserRemark());
        vo.setPlatformRemark(order.getPlatformRemark());
        vo.setInvoiceType(order.getInvoiceType());
        vo.setInvoiceStatus(order.getInvoiceStatus());
        vo.setInvoiceAttr(order.getInvoiceAttr());
        vo.setInvoiceTitle(order.getInvoiceTitle());
        vo.setInvoiceTaxNo(order.getInvoiceTaxNo());
        vo.setInvoiceContent(order.getInvoiceContent());
        vo.setInvoiceEmail(order.getInvoiceEmail());
        vo.setProducts(buildProducts(order));
        vo.setPayment(buildPayment(order));
        vo.setSteps(buildSteps(order));
        vo.setAfterSale(afterSale != null ? buildAfterSale(afterSale, defaultReturnAddress) : null);
        vo.setActions(buildActions(order, afterSale));
        return vo;
    }

    private static String buildShipStatusText(OrderEntity order) {
        if ("none".equals(order.getShipStatus()) || "not_shipped".equals(order.getShipStatus())) {
            return "未发货";
        }
        if ("signed".equals(order.getShipStatus())) {
            return order.getLogistics() != null ? "已签收(" + order.getLogistics() + ")" : "已签收";
        }
        if ("in_transit".equals(order.getShipStatus())) {
            return order.getLogistics() != null ? "派送中(" + order.getLogistics() + ")" : "运输中";
        }
        return order.getShipStatus();
    }

    private static String afterSalesStatusText(String status) {
        return switch (status) {
            case "refunded" -> "已退款";
            case "processing" -> "售后处理中";
            case "none" -> "未售后";
            default -> status;
        };
    }

    private static List<OrderDetailVO.ProductLineVO> buildProducts(OrderEntity order) {
        OrderDetailVO.ProductLineVO line = new OrderDetailVO.ProductLineVO();
        line.setThumb(order.getThumb());
        line.setName(order.getProductName());
        line.setSpec(order.getSpec());
        line.setQuantity(order.getQuantity());
        line.setSku(order.getProductSku());
        line.setUnitPrice(order.getUnitPrice());
        line.setPayable(order.getPayableSubtotal());
        line.setDiscount(order.getDiscountAmount());
        line.setFreight(order.getFreight());
        line.setSubtotal(order.getPayableSubtotal());
        BigDecimal paid = "pending_payment".equals(order.getOrderStatus())
                ? BigDecimal.ZERO : order.getActualAmount();
        line.setActualPaid(paid);
        return List.of(line);
    }

    private static OrderDetailVO.PaymentVO buildPayment(OrderEntity order) {
        OrderDetailVO.PaymentVO p = new OrderDetailVO.PaymentVO();
        p.setProductTotal(order.getProductTotal());
        p.setFreight(order.getFreight());
        p.setGoldCoin(BigDecimal.ZERO);
        p.setPromotion(BigDecimal.ZERO);
        p.setDiscount(BigDecimal.ZERO);
        p.setCoupon(order.getCouponAmount() != null ? order.getCouponAmount() : order.getDiscountAmount());
        p.setPayableSubtotal(order.getPayableSubtotal());
        p.setActualPaid("pending_payment".equals(order.getOrderStatus()) ? BigDecimal.ZERO : order.getActualAmount());
        return p;
    }

    private static List<OrderDetailVO.StepVO> buildSteps(OrderEntity order) {
        List<OrderDetailVO.StepVO> steps = new ArrayList<>();
        steps.add(step("提交订单", format(order.getCreateTime()), "finish", null));
        String payStatus = order.getPayTime() != null ? "finish" : "wait";
        steps.add(step("付款", format(order.getPayTime()), payStatus, payStatus.equals("wait") ? "未支付" : null));
        boolean shipped = "shipped".equals(order.getOrderStatus()) || "completed".equals(order.getOrderStatus())
                || "closed".equals(order.getOrderStatus());
        steps.add(step(shipped && order.getPayTime() != null ? "平台发货" : "平台发货",
                shipped ? format(order.getPayTime()) : "",
                shipped ? "finish" : "wait",
                shipped ? null : "未发货"));
        boolean signed = "signed".equals(order.getShipStatus()) || "completed".equals(order.getOrderStatus());
        steps.add(step(signed ? "已签收" : "用户收货", signed ? format(order.getAutoConfirmTime()) : "",
                signed ? "finish" : "wait", signed ? null : "未收货"));
        steps.add(step("完成评价", "", "wait", "未评价"));
        return steps;
    }

    private static OrderDetailVO.StepVO step(String title, String time, String status, String desc) {
        OrderDetailVO.StepVO s = new OrderDetailVO.StepVO();
        s.setTitle(title);
        s.setTime(time != null ? time : "");
        s.setStatus(status);
        s.setDesc(desc);
        return s;
    }

    private static OrderDetailVO.AfterSaleBlockVO buildAfterSale(AfterSaleEntity a, String returnAddress) {
        OrderDetailVO.AfterSaleBlockVO block = new OrderDetailVO.AfterSaleBlockVO();
        block.setId(a.getAfterSaleNo());
        block.setStatus(a.getAfterSaleStatus());
        block.setStatusText(AFTER_SALE_STATUS_TEXT.getOrDefault(a.getAfterSaleStatus(), a.getAfterSaleStatus()));
        block.setType(a.getAfterSaleType());
        block.setTypeText(AFTER_SALE_TYPE_TEXT.getOrDefault(a.getAfterSaleType(), a.getAfterSaleType()));
        block.setReason("用户申请售后");
        block.setDescription("");
        block.setEvidence("");
        block.setRefundAmount(a.getRefundAmount());
        block.setReturnLogistics("");
        block.setApplyTime(format(a.getApplyTime()));
        block.setAgreeTime(format(a.getProcessTime()));
        block.setRefundTime(format(a.getProcessTime()));
        block.setReturnAddress(returnAddress);
        return block;
    }

    private static List<String> buildActions(OrderEntity order, AfterSaleEntity afterSale) {
        List<String> actions = new ArrayList<>(buildBaseActions(order));
        if (afterSale != null) {
            if ("platform_pending".equals(afterSale.getAfterSaleStatus())) {
                actions.addAll(List.of("afterSaleApprove", "afterSaleReject"));
            } else if ("platform_confirm".equals(afterSale.getAfterSaleStatus())) {
                actions.add("afterSaleConfirmReturn");
            }
        }
        return actions;
    }

    private static List<String> buildBaseActions(OrderEntity order) {
        List<String> actions = new ArrayList<>(List.of("contact", "export"));
        if ("pending_payment".equals(order.getOrderStatus())) {
            actions.add("close");
            return actions;
        }
        if ("pending_ship".equals(order.getOrderStatus()) || "paid".equals(order.getOrderStatus())) {
            actions.addAll(List.of("download", "printShip", "printExpress"));
            return actions;
        }
        if ("shipped".equals(order.getOrderStatus())) {
            actions.addAll(List.of("download", "printShip", "printExpress", "reship"));
            return actions;
        }
        if ("completed".equals(order.getOrderStatus())) {
            actions.addAll(List.of("download", "printShip", "printExpress", "reship"));
            return actions;
        }
        actions.addAll(List.of("download", "printShip", "printExpress"));
        return actions;
    }

    private static String format(java.time.LocalDateTime time) {
        return time != null ? time.format(FMT) : "";
    }

    private static String nullToDash(String v) {
        return v != null && !v.isBlank() ? v : "未发货";
    }
}
