package com.dianshang.admin.order.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "oms_order_address")
public class OrderAddressEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "address_code", nullable = false, unique = true, length = 20)
    private String addressCode;

    /** ship | return */
    @Column(name = "address_type", nullable = false, length = 10)
    private String addressType;

    @Column(name = "contact_name", nullable = false, length = 50)
    private String contactName;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(length = 30)
    private String province;

    @Column(length = 30)
    private String city;

    @Column(length = 30)
    private String district;

    @Column(name = "province_code", length = 20)
    private String provinceCode;

    @Column(name = "city_code", length = 20)
    private String cityCode;

    @Column(name = "district_code", length = 20)
    private String districtCode;

    @Column(name = "detail_address", nullable = false, length = 300)
    private String detailAddress;

    @Column(name = "zip_code", length = 10)
    private String zipCode;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @Column(nullable = false)
    private Boolean visible = true;

    @Column(name = "sort_num", nullable = false)
    private Integer sortNum = 0;

    @Column(name = "add_time", nullable = false)
    private LocalDateTime addTime = LocalDateTime.now();

    @Column(nullable = false)
    private Boolean deleted = false;
}
