package com.dianshang.admin.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "pms_brand")
public class ProductBrand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "initial_char", length = 5)
    private String initialChar;

    @Column(name = "product_count", nullable = false)
    private Integer productCount = 0;

    @Column(length = 50)
    private String supplier;

    @Column(nullable = false)
    private Boolean visible = true;

    @Column(name = "sort_num", nullable = false)
    private Integer sortNum = 0;

    @Column(nullable = false)
    private Boolean deleted = false;
}
