package com.dianshang.admin.member.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ums_member_address")
public class MemberAddressEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_no", nullable = false, length = 20)
    private String userNo;

    @Column(name = "contact_name", length = 50)
    private String contactName;

    @Column(length = 20)
    private String phone;

    @Column(length = 200)
    private String region;

    @Column(name = "detail_address", length = 300)
    private String detailAddress;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;
}
