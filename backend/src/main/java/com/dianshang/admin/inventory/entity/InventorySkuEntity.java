package com.dianshang.admin.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "wms_inventory_sku")
public class InventorySkuEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_no", nullable = false, length = 20)
    private String productNo;

    @Column(name = "sku_line_id", nullable = false, length = 30)
    private String skuLineId;

    @Column(name = "sku_name", nullable = false, length = 100)
    private String skuName;

    @Column(name = "sku_code", length = 50)
    private String skuCode;

    @Column(name = "actual_stock", nullable = false)
    private Integer actualStock = 0;

    @Column(name = "warning_stock", nullable = false)
    private Integer warningStock = 0;

    @Column(name = "warehouse_code", length = 30)
    private String warehouseCode = "WH-001";

    @Column(nullable = false)
    private Boolean deleted = false;
}
