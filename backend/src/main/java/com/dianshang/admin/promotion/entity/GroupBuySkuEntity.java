package com.dianshang.admin.promotion.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "pms_group_buy_sku")
public class GroupBuySkuEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "activity_code", nullable = false, length = 20)
    private String activityCode;

    @Column(name = "slot_code", nullable = false, length = 20)
    private String slotCode;

    @Column(name = "product_no", nullable = false, length = 20)
    private String productNo;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "group_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal groupPrice = BigDecimal.ZERO;

    @Column(name = "group_size", nullable = false)
    private Integer groupSize = 2;

    @Column(name = "group_qty", nullable = false)
    private Integer groupQty = 0;

    @Column(name = "remain_stock", nullable = false)
    private Integer remainStock = 0;

    @Column(name = "warning_stock", nullable = false)
    private Integer warningStock = 0;

    @Column(name = "limit_qty", nullable = false)
    private Integer limitQty = 1;

    @Column(name = "sort_num", nullable = false)
    private Integer sortNum = 0;

    @Column(name = "attrs_json", columnDefinition = "CLOB")
    private String attrsJson;
}
