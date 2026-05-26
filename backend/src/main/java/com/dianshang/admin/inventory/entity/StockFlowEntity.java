package com.dianshang.admin.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "wms_stock_flow")
public class StockFlowEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "flow_no", nullable = false, unique = true, length = 20)
    private String flowNo;

    @Column(name = "product_no", nullable = false, length = 20)
    private String productNo;

    @Column(name = "related_no", length = 50)
    private String relatedNo;

    @Column(name = "order_id", length = 20)
    private String orderId;

    @Column(name = "sku_line_id", length = 30)
    private String skuLineId;

    @Column(name = "sku_name", length = 100)
    private String skuName;

    @Column(name = "flow_type", nullable = false, length = 30)
    private String flowType;

    @Column(name = "before_qty", nullable = false)
    private Integer beforeQty;

    @Column(name = "change_qty", nullable = false)
    private Integer changeQty;

    @Column(name = "after_qty", nullable = false)
    private Integer afterQty;

    @Column(name = "operator_name", length = 50)
    private String operatorName;

    @Column(length = 300)
    private String remark;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
