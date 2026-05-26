package com.dianshang.admin.member.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class MemberDetailVO {

    private String userId;
    private String nickname;
    private String avatar;
    private String phoneMasked;
    private String level;
    private String levelKey;
    private String statusText;
    private String ip;
    private String source;
    private String tags;
    private String birthday;
    private String registerTime;
    private String city;
    private String gender;
    private BigDecimal consumeAmount;
    private Integer orderCount;
    private Integer points;
    private Integer growth;
    private Integer couponCount;
    private Integer reviewCount;
    private Integer returnCount;
    private Integer loginCount;
    private Integer favoriteProducts;
    private Integer favoriteTopics;
    private Integer orderFriends;
    private Integer lotteryCount;
    private String remark;
    private String account;
    private List<String> permissions;
    private List<MemberAddressVO> addresses;
    private List<MemberOrderBriefVO> orders;
}
