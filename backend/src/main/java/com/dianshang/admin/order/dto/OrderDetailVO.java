package com.dianshang.admin.order.dto;

import com.dianshang.admin.system.dto.PlatformHintsVO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderDetailVO {

    private String id;
    private String orderStatus;
    private String orderStatusText;
    private String shipStatus;
    private String shipStatusText;
    private String afterSalesStatus;
    private String afterSalesStatusText;
    private String deliverySerial;
    private String userAccount;
    private String payMethod;
    private String payMethodDetail;
    private String orderSource;
    private String orderType;
    private String deliveryMethod;
    private String logisticsNo;
    private String autoConfirmDays;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String userRemark;
    private String platformRemark;
    private String invoiceType;
    private String invoiceStatus;
    private String invoiceAttr;
    private String invoiceTitle;
    private String invoiceTaxNo;
    private String invoiceContent;
    private String invoiceEmail;
    private List<ProductLineVO> products;
    private PaymentVO payment;
    private List<StepVO> steps;
    private AfterSaleBlockVO afterSale;
    private List<String> actions;
    private PlatformHintsVO platformHints;

    @Data
    public static class ProductLineVO {
        private String thumb;
        private String name;
        private String spec;
        private Integer quantity;
        private String sku;
        private BigDecimal unitPrice;
        private BigDecimal payable;
        private BigDecimal discount;
        private BigDecimal freight;
        private BigDecimal subtotal;
        private BigDecimal actualPaid;
    }

    @Data
    public static class PaymentVO {
        private BigDecimal productTotal;
        private BigDecimal freight;
        private BigDecimal goldCoin;
        private BigDecimal promotion;
        private BigDecimal discount;
        private BigDecimal coupon;
        private BigDecimal payableSubtotal;
        private BigDecimal actualPaid;
        private String freightNote;
    }

    @Data
    public static class StepVO {
        private String title;
        private String time;
        private String status;
        private String desc;
    }

    @Data
    public static class AfterSaleBlockVO {
        private String type;
        private String typeText;
        private String reason;
        private String description;
        private String evidence;
        private BigDecimal refundAmount;
        private String returnLogistics;
        private String applyTime;
        private String agreeTime;
        private String refundTime;
        private String returnAddress;
    }
}
