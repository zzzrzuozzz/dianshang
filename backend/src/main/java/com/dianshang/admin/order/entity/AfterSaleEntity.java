package com.dianshang.admin.order.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "oms_after_sale")
public class AfterSaleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "after_sale_no", nullable = false, unique = true, length = 20)
    private String afterSaleNo;

    @Column(name = "order_no", nullable = false, length = 20)
    private String orderNo;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(length = 500)
    private String thumb;

    @Column(name = "order_status", length = 30)
    private String orderStatus;

    @Column(name = "ship_status", length = 30)
    private String shipStatus;

    @Column(name = "after_sale_status", nullable = false, length = 30)
    private String afterSaleStatus;

    @Column(name = "after_sale_type", nullable = false, length = 30)
    private String afterSaleType;

    @Column(name = "refund_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal refundAmount = BigDecimal.ZERO;

    @Column(name = "apply_time")
    private LocalDateTime applyTime;

    @Column(name = "process_time")
    private LocalDateTime processTime;

    @Column(nullable = false)
    private Boolean deleted = false;
}
