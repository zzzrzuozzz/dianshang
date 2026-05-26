package com.dianshang.admin.member.dto;

import lombok.Data;

@Data
public class GrowthRewardsVO {

    private Boolean registerEnabled;
    private Integer registerGrowth;
    private Integer registerPoints;
    private Boolean birthdayEnabled;
    private Integer birthdayGrowth;
    private Integer birthdayPoints;
    private Boolean inviteEnabled;
    private Integer inviteGrowth;
    private Integer invitePoints;
}
