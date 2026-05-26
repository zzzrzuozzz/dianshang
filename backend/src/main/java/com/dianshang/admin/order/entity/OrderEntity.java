package com.dianshang.admin.order.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "oms_order")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", nullable = false, unique = true, length = 20)
    private String orderNo;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(length = 500)
    private String thumb;

    @Column(length = 100)
    private String spec;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(name = "actual_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal actualAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal freight = BigDecimal.ZERO;

    @Column(name = "freight_free", nullable = false)
    private Boolean freightFree = false;

    @Column(name = "order_status", nullable = false, length = 30)
    private String orderStatus;

    @Column(name = "ship_status", nullable = false, length = 30)
    private String shipStatus;

    @Column(length = 200)
    private String logistics;

    @Column(name = "pay_time")
    private LocalDateTime payTime;

    @Column(name = "auto_confirm_time")
    private LocalDateTime autoConfirmTime;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime = LocalDateTime.now();

    @Column(name = "receiver_name", length = 50)
    private String receiverName;

    @Column(name = "receiver_phone", length = 20)
    private String receiverPhone;

    @Column(name = "receiver_address", length = 300)
    private String receiverAddress;

    @Column(length = 50)
    private String supplier;

    @Column(name = "supplier_phone", length = 50)
    private String supplierPhone;

    @Column(name = "user_account", length = 100)
    private String userAccount;

    @Column(name = "pay_method", length = 50)
    private String payMethod;

    @Column(name = "pay_method_detail", length = 100)
    private String payMethodDetail;

    @Column(name = "order_source", length = 20)
    private String orderSource;

    @Column(name = "order_type", length = 30)
    private String orderType;

    @Column(name = "delivery_method", length = 30)
    private String deliveryMethod;

    @Column(name = "logistics_no", length = 80)
    private String logisticsNo;

    @Column(name = "delivery_serial", length = 50)
    private String deliverySerial;

    @Column(name = "auto_confirm_days_label", length = 50)
    private String autoConfirmDaysLabel;

    @Column(name = "user_remark", length = 500)
    private String userRemark;

    @Column(name = "platform_remark", length = 500)
    private String platformRemark;

    @Column(name = "invoice_type", length = 30)
    private String invoiceType;

    @Column(name = "invoice_status", length = 30)
    private String invoiceStatus;

    @Column(name = "invoice_attr", length = 30)
    private String invoiceAttr;

    @Column(name = "invoice_title", length = 100)
    private String invoiceTitle;

    @Column(name = "invoice_tax_no", length = 50)
    private String invoiceTaxNo;

    @Column(name = "invoice_content", length = 100)
    private String invoiceContent;

    @Column(name = "invoice_email", length = 100)
    private String invoiceEmail;

    @Column(name = "product_sku", length = 50)
    private String productSku;

    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "product_total", precision = 10, scale = 2)
    private BigDecimal productTotal;

    @Column(name = "coupon_amount", precision = 10, scale = 2)
    private BigDecimal couponAmount;

    @Column(name = "payable_subtotal", precision = 10, scale = 2)
    private BigDecimal payableSubtotal;

    @Column(name = "after_sales_status", nullable = false, length = 30)
    private String afterSalesStatus = "none";

    @Column(name = "product_no", length = 20)
    private String productNo;

    @Column(nullable = false)
    private Boolean deleted = false;
}
