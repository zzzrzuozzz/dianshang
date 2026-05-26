package com.dianshang.admin.promotion.support;

import com.dianshang.admin.common.BusinessException;
import com.dianshang.admin.promotion.entity.SeckillActivityEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Map;

public final class PromotionSupport {

    private static final DateTimeFormatter OUT = DateTimeFormatter.ofPattern("yyyy-M-d HH:mm");

    private static final Map<String, String> COUPON_TYPE_LABEL = Map.of(
            "newcomer", "新人券",
            "shopping", "购物赠券",
            "member", "会员赠券",
            "sitewide", "全场赠券"
    );

    private PromotionSupport() {
    }

    public static String formatTime(LocalDateTime time) {
        return time == null ? "" : time.format(OUT);
    }

    public static LocalDateTime parseDateTime(String text) {
        if (text == null || text.isBlank()) {
            throw new BusinessException("时间不能为空");
        }
        String s = text.trim().replace('T', ' ');
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern("yyyy-M-d")
                .optionalStart()
                .appendPattern(" HH:mm:ss")
                .optionalEnd()
                .optionalStart()
                .appendPattern(" HH:mm")
                .optionalEnd()
                .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                .toFormatter();
        try {
            return LocalDateTime.parse(s, formatter);
        } catch (Exception e) {
            throw new BusinessException("时间格式不正确: " + text);
        }
    }

    public static String resolveActivityStatus(LocalDateTime start, LocalDateTime end, boolean online) {
        if (!online) {
            return "offline";
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(start)) {
            return "pending";
        }
        if (now.isAfter(end)) {
            return "ended";
        }
        return "active";
    }

    public static String couponTypeLabel(String type) {
        return COUPON_TYPE_LABEL.getOrDefault(type, type);
    }

    public static String thresholdText(java.math.BigDecimal threshold, java.math.BigDecimal face) {
        return "满" + threshold.stripTrailingZeros().toPlainString()
                + "减" + face.stripTrailingZeros().toPlainString() + "元";
    }

    public static void refreshSeckillWarning(SeckillActivityEntity activity, long lowStockCount) {
        if (lowStockCount > 0) {
            activity.setWarningMsg("有" + lowStockCount + "款商品库存低于预警值");
        } else {
            activity.setWarningMsg(null);
        }
    }
}
