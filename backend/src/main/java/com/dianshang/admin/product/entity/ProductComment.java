package com.dianshang.admin.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "pms_product_comment")
public class ProductComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_no", nullable = false, length = 20)
    private String productNo;

    @Column(nullable = false, length = 10)
    private String rating;

    @Column(length = 1000)
    private String content;

    @Column(nullable = false)
    private Boolean featured = false;

    @Column(nullable = false)
    private Boolean deleted = false;
}
