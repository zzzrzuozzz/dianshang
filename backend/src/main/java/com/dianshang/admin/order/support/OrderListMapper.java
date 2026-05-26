package com.dianshang.admin.order.support;

import com.dianshang.admin.order.dto.OrderListVO;
import com.dianshang.admin.order.entity.OrderEntity;

import java.time.format.DateTimeFormatter;

public final class OrderListMapper {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-M-d HH:mm:ss");

    private OrderListMapper() {
    }

    public static OrderListVO toVO(OrderEntity o) {
        OrderListVO vo = new OrderListVO();
        vo.setId(o.getOrderNo());
        vo.setProductName(o.getProductName());
        vo.setThumb(o.getThumb());
        vo.setSpec(o.getSpec());
        vo.setQuantity(o.getQuantity());
        vo.setActualAmount(o.getActualAmount());
        vo.setDiscount(o.getDiscountAmount());
        vo.setFreight(o.getFreight());
        vo.setFreightFree(o.getFreightFree());
        vo.setOrderStatus(o.getOrderStatus());
        vo.setShipStatus(o.getShipStatus());
        vo.setLogistics(o.getLogistics() != null ? o.getLogistics() : "");
        vo.setPayTime(format(o.getPayTime()));
        vo.setAutoConfirmTime(format(o.getAutoConfirmTime()));
        vo.setReceiverName(o.getReceiverName());
        vo.setReceiverPhone(o.getReceiverPhone());
        vo.setReceiverAddress(o.getReceiverAddress());
        vo.setSupplier(o.getSupplier());
        vo.setSupplierPhone(o.getSupplierPhone());
        return vo;
    }

    private static String format(java.time.LocalDateTime time) {
        return time != null ? time.format(FMT) : "";
    }
}
