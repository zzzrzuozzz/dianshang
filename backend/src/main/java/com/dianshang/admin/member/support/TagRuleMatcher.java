package com.dianshang.admin.member.support;

import com.dianshang.admin.member.dto.MemberTagSaveRequest;
import com.dianshang.admin.member.entity.MemberEntity;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public final class TagRuleMatcher {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private TagRuleMatcher() {
    }

    public static boolean matches(MemberTagSaveRequest rule, MemberEntity member) {
        if (rule == null || member == null) {
            return false;
        }
        if (!matchGender(rule.getGender(), member.getGender())) {
            return false;
        }
        if (!matchMemberLevels(rule.getMemberLevels(), member.getLevelCode())) {
            return false;
        }
        if (!matchCity(rule.getCityMode(), rule.getRegionCodes(), member.getCityCodes())) {
            return false;
        }
        if (Boolean.TRUE.equals(rule.getOrderCountEnabled())
                && "cumulative".equals(rule.getOrderCountType())
                && rule.getOrderCount() != null
                && member.getOrderCount() < rule.getOrderCount()) {
            return false;
        }
        if (Boolean.TRUE.equals(rule.getAmountEnabled())
                && "cumulative".equals(rule.getAmountType())
                && rule.getAmount() != null
                && member.getConsumeAmount().doubleValue() < rule.getAmount()) {
            return false;
        }
        return true;
    }

    private static boolean matchGender(List<String> genders, String memberGender) {
        if (genders == null || genders.isEmpty() || genders.contains("all")) {
            return true;
        }
        String g = memberGender == null ? "secret" : memberGender;
        return genders.contains(g);
    }

    private static boolean matchMemberLevels(List<String> levels, String levelCode) {
        if (levels == null || levels.isEmpty() || levels.contains("all")) {
            return true;
        }
        return levels.contains(levelCode);
    }

    private static boolean matchCity(String cityMode, List<List<String>> regionCodes, String memberCityCodes) {
        if (cityMode == null || "all".equals(cityMode)) {
            return true;
        }
        if (regionCodes == null || regionCodes.isEmpty()) {
            return true;
        }
        if (memberCityCodes == null || memberCityCodes.isBlank()) {
            return false;
        }
        String codes = memberCityCodes;
        for (List<String> path : regionCodes) {
            if (path == null || path.isEmpty()) {
                continue;
            }
            String prefix = String.join(",", path);
            if (codes.startsWith(prefix) || codes.contains(prefix)) {
                return true;
            }
        }
        return false;
    }

    public static String buildConditionText(MemberTagSaveRequest rule) {
        StringBuilder sb = new StringBuilder();
        if (Boolean.TRUE.equals(rule.getOrderCountEnabled()) && rule.getOrderCount() != null) {
            sb.append("累计成功交易: ").append(rule.getOrderCount()).append("笔  ");
        }
        if (Boolean.TRUE.equals(rule.getAmountEnabled()) && rule.getAmount() != null) {
            sb.append("累计购买金额: ¥").append(String.format("%.2f", rule.getAmount()));
        }
        if (sb.length() == 0) {
            return "手动/无条件标签";
        }
        return sb.toString().trim();
    }

    public static MemberTagSaveRequest parseRule(String json) {
        if (json == null || json.isBlank()) {
            return new MemberTagSaveRequest();
        }
        try {
            return MAPPER.readValue(json, MemberTagSaveRequest.class);
        } catch (Exception e) {
            return new MemberTagSaveRequest();
        }
    }

    public static String toRuleJson(MemberTagSaveRequest rule) {
        try {
            return MAPPER.writeValueAsString(rule);
        } catch (Exception e) {
            return "{}";
        }
    }
}
