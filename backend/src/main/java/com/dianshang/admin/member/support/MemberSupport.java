package com.dianshang.admin.member.support;

import com.dianshang.admin.common.Jsons;
import com.dianshang.admin.member.dto.MemberListVO;
import com.dianshang.admin.member.entity.MemberEntity;
import com.dianshang.admin.member.entity.MemberLevelEntity;
import com.dianshang.admin.member.repository.MemberLevelRepository;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MemberSupport {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-M-d HH:mm:ss");

    private static final Map<String, String> STATUS_TEXT = Map.of(
            "normal", "正常",
            "withdraw_banned", "禁止提现",
            "login_banned", "禁止登录",
            "lottery_banned", "禁止抽奖",
            "order_banned", "禁止下单"
    );

    private static final Map<String, String> GENDER_TEXT = Map.of(
            "male", "男",
            "female", "女",
            "secret", "保密"
    );

    private MemberSupport() {
    }

    public static String statusText(String status) {
        return STATUS_TEXT.getOrDefault(status, status);
    }

    public static String genderText(String gender) {
        if (gender == null) {
            return "-";
        }
        return GENDER_TEXT.getOrDefault(gender, gender);
    }

    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    public static String formatTime(java.time.LocalDateTime time) {
        return time == null ? "" : time.format(DT);
    }

    public static Map<String, String> levelNameMap(List<MemberLevelEntity> levels) {
        Map<String, String> map = new HashMap<>();
        for (MemberLevelEntity lv : levels) {
            map.put(lv.getLevelCode(), lv.getLevelName());
        }
        return map;
    }

    public static MemberListVO toListVO(MemberEntity m, Map<String, String> levelNames) {
        MemberListVO vo = new MemberListVO();
        vo.setId(m.getUserNo());
        vo.setNickname(m.getNickname());
        vo.setAccount(m.getPhone());
        vo.setLevelKey(m.getLevelCode());
        vo.setLevel(levelNames.getOrDefault(m.getLevelCode(), m.getLevelCode()));
        vo.setConsumeAmount(m.getConsumeAmount());
        vo.setOrderCount(m.getOrderCount());
        vo.setPoints(m.getPoints());
        vo.setGrowth(m.getGrowthValue());
        vo.setStatus(m.getStatus());
        vo.setStatusText(statusText(m.getStatus()));
        vo.setRemark(m.getRemark() == null || m.getRemark().isBlank() ? "-" : m.getRemark());
        vo.setRegisterTime(formatTime(m.getRegisterTime()));
        vo.setAvatar(m.getAvatar());
        return vo;
    }

    public static List<String> parsePermissions(String json) {
        return Jsons.toStringList(json);
    }

    public static String resolvePrimaryStatus(List<String> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return "normal";
        }
        if (permissions.contains("login_banned")) {
            return "login_banned";
        }
        if (permissions.contains("withdraw_banned")) {
            return "withdraw_banned";
        }
        if (permissions.contains("order_banned")) {
            return "order_banned";
        }
        if (permissions.contains("lottery_banned")) {
            return "lottery_banned";
        }
        return "normal";
    }

    public static String freeShippingText(MemberLevelEntity lv) {
        if (lv.getFreeShipAmount() == null) {
            return "-";
        }
        return lv.getFreeShipAmount().stripTrailingZeros().toPlainString()
                + "元包邮/每月" + (lv.getFreeShipTimes() == null ? 0 : lv.getFreeShipTimes()) + "次";
    }

    public static String reviewRewardText(MemberLevelEntity lv) {
        return "+" + (lv.getReviewGrowth() == null ? 0 : lv.getReviewGrowth())
                + "成长值/条";
    }
}
