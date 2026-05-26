package com.dianshang.admin.config;

import com.dianshang.admin.common.Jsons;
import com.dianshang.admin.ops.advertisement.entity.OpsAdvertisementEntity;
import com.dianshang.admin.ops.advertisement.repository.OpsAdvertisementRepository;
import com.dianshang.admin.ops.notification.entity.OpsNotificationEntity;
import com.dianshang.admin.ops.notification.repository.OpsNotificationRepository;
import com.dianshang.admin.ops.support.OpsAudienceSupport;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class OpsDataInitializer implements CommandLineRunner {

    private final OpsNotificationRepository notificationRepository;
    private final OpsAdvertisementRepository advertisementRepository;

    public OpsDataInitializer(OpsNotificationRepository notificationRepository,
                              OpsAdvertisementRepository advertisementRepository) {
        this.notificationRepository = notificationRepository;
        this.advertisementRepository = advertisementRepository;
    }

    @Override
    public void run(String... args) {
        // 通知表在 dev 下可能被 schema-ops-repair 重建，与广告表种子需分别判断
        if (notificationRepository.count() == 0) {
            seedNotifications();
        }
        if (advertisementRepository.count() == 0) {
            seedAdvertisements();
        }
    }

    private void seedNotifications() {
        saveSystem("BZ6542", "春季家电家具疯狂秒杀", "ops", "TEXT", null, null,
                LocalDateTime.of(2024, 8, 8, 16, 14), 1, 1000, 500, 1000, "200位", true);
        saveSystem("BZ6543", "新用户专享优惠券到账提醒", "system", "INNER", "coupon", "/pages/coupon",
                LocalDateTime.of(2024, 8, 10, 9, 0), 0, 0, 0, 0, "-", false);
        saveSystem("BZ6544", "您的订单已发货，请注意查收", "service", "INNER", "product", "/pages/order/detail",
                LocalDateTime.of(2024, 8, 6, 14, 30), 1, 800, 320, 780, "13066660001", true);

        saveSms("BZ6550", "【商城】您的验证码为 886622，5分钟内有效。", 1, 1200, "1200位");
        saveSms("BZ6551", "【商城】您有一张满200减30优惠券即将过期。", 0, 0, "-");

        saveStation("BZ6560", "会员日专属福利已到账", "恭喜获得 100 积分，快去会员中心查看。", 1, 600, "600位");
        saveStation("BZ6561", "订单签收提醒", "您的包裹已签收，欢迎评价。", 1, 400, "400位");
    }

    private void saveSystem(String code, String title, String category, String jumpType,
                            String innerLink, String jumpUrl, LocalDateTime publishedAt,
                            int status, int volume, int clicks, int receive, String pushUser, boolean appPush) {
        if (notificationRepository.findByNotifyCodeAndDeletedFalse(code).isPresent()) {
            return;
        }
        OpsNotificationEntity e = baseNotify(code, "SYSTEM", title, category, publishedAt, status, volume, clicks, receive, pushUser, appPush);
        e.setJumpType(jumpType);
        e.setInnerLinkType(innerLink);
        e.setJumpUrl(jumpUrl);
        e.setIntro("运营活动推送");
        notificationRepository.save(e);
    }

    private void saveSms(String code, String content, int status, int volume, String pushUser) {
        if (notificationRepository.findByNotifyCodeAndDeletedFalse(code).isPresent()) {
            return;
        }
        OpsNotificationEntity e = baseNotify(code, "SMS", content.length() > 30 ? content.substring(0, 30) : content,
                null, LocalDateTime.now().minusDays(1), status, volume, volume / 3, volume, pushUser, false);
        e.setContent(content);
        notificationRepository.save(e);
    }

    private void saveStation(String code, String title, String content, int status, int volume, String pushUser) {
        if (notificationRepository.findByNotifyCodeAndDeletedFalse(code).isPresent()) {
            return;
        }
        OpsNotificationEntity e = baseNotify(code, "STATION", title, null, LocalDateTime.now().minusHours(6),
                status, volume, volume / 4, volume, pushUser, false);
        e.setContent(content);
        e.setAudienceJson(OpsAudienceSupport.buildAudienceJson(
                List.of("all"), List.of(), defaultTags(), List.of("assign", "tag")));
        notificationRepository.save(e);
    }

    private OpsNotificationEntity baseNotify(String code, String msgType, String title, String category,
                                             LocalDateTime publishedAt, int status, int volume, int clicks,
                                             int receive, String pushUser, boolean appPush) {
        OpsNotificationEntity e = new OpsNotificationEntity();
        e.setNotifyCode(code);
        e.setMsgType(msgType);
        e.setMsgCategory(category);
        e.setTitle(title);
        e.setSendType(1);
        e.setAppPush(appPush);
        e.setPublishStatus(status);
        e.setPublishedAt(status == 1 ? publishedAt : null);
        e.setPushCount(status == 1 ? 1 : 0);
        e.setPushVolume(volume);
        e.setClickCount(clicks);
        e.setReceiveVolume(receive);
        e.setPushUserText(pushUser);
        e.setEstimatedUsers(volume);
        e.setAudienceJson(OpsAudienceSupport.buildAudienceJson(List.of("all"), List.of(), defaultTags(), null));
        e.setCoverImagesJson(Jsons.toJson(List.of()));
        e.setCreatedAt(publishedAt == null ? LocalDateTime.now() : publishedAt);
        e.setDeleted(false);
        return e;
    }

    private Map<String, List<String>> defaultTags() {
        return Map.of(
                "newUser", List.of("all"),
                "firstBuy", List.of("all"),
                "repurchase", List.of("all"),
                "active", List.of("all")
        );
    }

    private void seedAdvertisements() {
        saveAdv("BZ6542", "carousel", "春季家电家具疯狂秒杀",
                LocalDateTime.of(2024, 8, 8, 16, 14), LocalDateTime.of(2024, 8, 18, 16, 14),
                true, 1000, 1000, 2);
        saveAdv("BZ6543", "guess", "夏日清凉专场",
                LocalDateTime.of(2024, 8, 1, 10, 0), LocalDateTime.of(2024, 8, 31, 22, 0),
                false, 5000, 800, 1);
    }

    private void saveAdv(String code, String type, String title, LocalDateTime start, LocalDateTime end,
                         boolean online, int exposure, int clicks, int sort) {
        if (advertisementRepository.findByAdvCodeAndDeletedFalse(code).isPresent()) {
            return;
        }
        OpsAdvertisementEntity e = new OpsAdvertisementEntity();
        e.setAdvCode(code);
        e.setAdvType(type);
        e.setTitle(title);
        e.setIntro("首页推荐");
        e.setJumpType("INNER");
        e.setJumpUrl("/pages/activity");
        e.setStartTime(start);
        e.setEndTime(end);
        e.setOnline(online);
        e.setExposureCount(exposure);
        e.setClickCount(clicks);
        e.setSortNum(sort);
        e.setAudienceJson(OpsAudienceSupport.buildAudienceJson(List.of("all"), List.of(), defaultTags(), null));
        e.setCreatedAt(start);
        e.setDeleted(false);
        advertisementRepository.save(e);
    }
}
