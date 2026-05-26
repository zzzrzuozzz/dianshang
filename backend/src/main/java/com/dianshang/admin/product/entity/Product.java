package com.dianshang.admin.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "pms_product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_no", nullable = false, unique = true, length = 20)
    private String productNo;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 200)
    private String subtitle;

    @Column(length = 500)
    private String thumb;

    @Column(name = "original_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal originalPrice = BigDecimal.ZERO;

    @Column(name = "discount_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountPrice = BigDecimal.ZERO;

    @Column(nullable = false, length = 10)
    private String status = "off";

    @Column(name = "audit_status", nullable = false, length = 20)
    private String auditStatus = "pending";

    @Column(length = 500)
    private String remark;

    @Column(length = 50)
    private String sku;

    @Column(name = "sort_num", nullable = false)
    private Integer sortNum = 0;

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(name = "month_sales", nullable = false)
    private Integer monthSales = 0;

    @Column(name = "total_sales", nullable = false)
    private Integer totalSales = 0;

    @Column(length = 50)
    private String supplier;

    @Column(name = "category_code", length = 50)
    private String categoryCode;

    @Column(name = "brand_code", length = 50)
    private String brandCode;

    @Column(nullable = false)
    private Boolean deleted = false;
}
