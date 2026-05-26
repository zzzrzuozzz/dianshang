package com.dianshang.admin.member.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "ums_member")
public class MemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_no", nullable = false, unique = true, length = 20)
    private String userNo;

    @Column(length = 100)
    private String nickname;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(length = 500)
    private String avatar;

    @Column(name = "level_code", length = 30)
    private String levelCode;

    @Column(nullable = false)
    private Integer points = 0;

    @Column(name = "growth_value", nullable = false)
    private Integer growthValue = 0;

    @Column(name = "consume_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal consumeAmount = BigDecimal.ZERO;

    @Column(name = "order_count", nullable = false)
    private Integer orderCount = 0;

    @Column(nullable = false, length = 30)
    private String status = "normal";

    @Column(length = 500)
    private String remark;

    @Column(length = 20)
    private String gender;

    @Column(length = 100)
    private String city;

    @Column(name = "city_codes", length = 200)
    private String cityCodes;

    @Column(length = 20)
    private String birthday;

    @Column(name = "register_time")
    private LocalDateTime registerTime;

    @Column(length = 30)
    private String source;

    @Column(name = "permissions_json", columnDefinition = "CLOB")
    private String permissionsJson;

    @Column(name = "coupon_count", nullable = false)
    private Integer couponCount = 0;

    @Column(name = "review_count", nullable = false)
    private Integer reviewCount = 0;

    @Column(name = "return_count", nullable = false)
    private Integer returnCount = 0;

    @Column(name = "login_count", nullable = false)
    private Integer loginCount = 0;

    @Column(name = "favorite_products", nullable = false)
    private Integer favoriteProducts = 0;

    @Column(name = "favorite_topics", nullable = false)
    private Integer favoriteTopics = 0;

    @Column(name = "order_friends", nullable = false)
    private Integer orderFriends = 0;

    @Column(name = "lottery_count", nullable = false)
    private Integer lotteryCount = 0;

    @Column(name = "last_ip", length = 50)
    private String lastIp;

    @Column(nullable = false)
    private Boolean deleted = false;
}
