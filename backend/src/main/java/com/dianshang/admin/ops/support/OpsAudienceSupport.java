package com.dianshang.admin.ops.support;

import com.dianshang.admin.common.Jsons;
import com.dianshang.admin.member.entity.MemberEntity;
import com.dianshang.admin.member.repository.MemberRepository;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class OpsAudienceSupport {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private OpsAudienceSupport() {
    }

    public static String buildAudienceJson(List<String> memberLevels,
                                           List<List<String>> regions,
                                           Map<String, List<String>> tags,
                                           List<String> pushMethod) {
        Map<String, Object> map = new HashMap<>();
        map.put("memberLevels", memberLevels != null ? memberLevels : List.of());
        map.put("regions", regions != null ? regions : List.of());
        map.put("tags", sanitizeTags(tags));
        if (pushMethod != null && !pushMethod.isEmpty()) {
            map.put("pushMethod", pushMethod);
        }
        return Jsons.toJson(map);
    }

    private static Map<String, List<String>> sanitizeTags(Map<String, List<String>> tags) {
        if (tags == null || tags.isEmpty()) {
            return Map.of();
        }
        Map<String, List<String>> safe = new HashMap<>();
        tags.forEach((key, value) -> {
            if (key != null && value != null) {
                safe.put(key, value);
            }
        });
        return safe;
    }

    @SuppressWarnings("unchecked")
    public static void fillAudienceFields(Map<String, Object> audience,
                                          List<String> memberLevelsOut,
                                          List<List<String>> regionsOut,
                                          Map<String, List<String>> tagsOut,
                                          List<String> pushMethodOut) {
        if (audience == null) {
            return;
        }
        Object levels = audience.get("memberLevels");
        if (levels instanceof List<?> list) {
            memberLevelsOut.addAll((List<String>) list);
        }
        Object regions = audience.get("regions");
        if (regions instanceof List<?> list) {
            regionsOut.addAll((List<List<String>>) list);
        }
        Object tags = audience.get("tags");
        if (tags instanceof Map<?, ?> map) {
            map.forEach((k, v) -> {
                if (v instanceof List<?> tagList) {
                    tagsOut.put(String.valueOf(k), (List<String>) tagList);
                }
            });
        }
        Object pushMethod = audience.get("pushMethod");
        if (pushMethod instanceof List<?> list) {
            pushMethodOut.addAll((List<String>) list);
        }
    }

    public static Map<String, Object> parseAudience(String json) {
        return Jsons.readValue(json, MAP_TYPE);
    }

    public static int estimateUsers(MemberRepository memberRepository,
                                    List<String> memberLevels,
                                    List<List<String>> regions,
                                    Map<String, List<String>> tags) {
        List<MemberEntity> members = memberRepository.findByDeletedFalse();
        if (members.isEmpty()) {
            return 0;
        }
        long count = members.stream()
                .filter(m -> m != null)
                .filter(m -> matchLevel(m, memberLevels))
                .count();
        if (count == 0) {
            count = members.size();
        }
        double factor = 1.0;
        if (regions != null && !regions.isEmpty()) {
            factor *= 0.82;
        }
        if (tags != null && tags.values().stream().anyMatch(OpsAudienceSupport::isRestrictiveTag)) {
            factor *= 0.75;
        }
        return Math.max(1, (int) Math.round(count * factor));
    }

    private static boolean matchLevel(MemberEntity member, List<String> levels) {
        if (levels == null || levels.isEmpty() || levels.contains("all")) {
            return true;
        }
        String code = member.getLevelCode() == null ? "normal" : member.getLevelCode();
        return levels.contains(code);
    }

    private static boolean isRestrictiveTag(List<String> values) {
        if (values == null || values.isEmpty()) {
            return false;
        }
        return values.stream().anyMatch(v -> v != null && !v.isBlank() && !"all".equals(v) && !"none".equals(v));
    }
}
