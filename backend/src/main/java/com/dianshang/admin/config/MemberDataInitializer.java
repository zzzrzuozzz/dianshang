package com.dianshang.admin.config;

import com.dianshang.admin.common.Jsons;
import com.dianshang.admin.member.dto.MemberTagSaveRequest;
import com.dianshang.admin.member.entity.*;
import com.dianshang.admin.member.repository.*;
import com.dianshang.admin.member.service.MemberService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class MemberDataInitializer implements CommandLineRunner {

    private static final String AVATAR =
            "https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png";

    private final MemberRepository memberRepository;
    private final MemberLevelRepository levelRepository;
    private final MemberTagRepository tagRepository;
    private final MemberAddressRepository addressRepository;
    private final GrowthTaskRepository taskRepository;
    private final MemberService memberService;

    public MemberDataInitializer(MemberRepository memberRepository,
                                 MemberLevelRepository levelRepository,
                                 MemberTagRepository tagRepository,
                                 MemberAddressRepository addressRepository,
                                 GrowthTaskRepository taskRepository,
                                 MemberService memberService) {
        this.memberRepository = memberRepository;
        this.levelRepository = levelRepository;
        this.tagRepository = tagRepository;
        this.addressRepository = addressRepository;
        this.taskRepository = taskRepository;
        this.memberService = memberService;
    }

    @Override
    public void run(String... args) {
        if (memberRepository.count() > 0) {
            return;
        }
        seedLevels();
        seedMembers();
        seedAddresses();
        seedTags();
        seedTasks();
        memberService.saveRules(memberService.getRules());
        memberService.saveRewards(memberService.getRewards());
    }

    private void seedLevels() {
        saveLevel("normal", "普通会员", true, 1, 40, 2, 5, 10, 1);
        saveLevel("gold", "黄金会员", false, 500, 30, 3, 3, 10, 2);
        saveLevel("platinum", "铂金会员", false, 1000, 20, 4, 2, 10, 3);
        saveLevel("diamond", "钻石会员", false, 2000, 15, 5, 1, 10, 4);
    }

    private void saveLevel(String code, String name, boolean isDefault, int growthPoint,
                           int shipAmt, int shipTimes, int reviewGrowth, int reviewTimes, int sort) {
        MemberLevelEntity lv = new MemberLevelEntity();
        lv.setLevelCode(code);
        lv.setLevelName(name);
        lv.setIsDefault(isDefault);
        lv.setGrowthPoint(growthPoint);
        lv.setFreeShipAmount(BigDecimal.valueOf(shipAmt));
        lv.setFreeShipTimes(shipTimes);
        lv.setReviewGrowth(reviewGrowth);
        lv.setReviewTimes(reviewTimes);
        lv.setSortNum(sort);
        lv.setPrivilegesJson(Jsons.toJson(Map.of(
                "freeShip", true, "checkIn", true, "review", true,
                "exclusive", true, "specialPrice", true, "birthday", true)));
        levelRepository.save(lv);
    }

    private void seedMembers() {
        saveMember("BZ6542", "李天霸", "13088888888", "normal", 1000, 10, 200, 200,
                "normal", "-", "female", "深圳", "440000,440300", "2002-6-22");
        saveMember("BZ6543", "王小明", "13800138000", "gold", 5600, 45, 1200, 800,
                "normal", "-", "male", "北京", "110000,110105", "1995-3-10");
        saveMember("BZ6544", "违规用户", "13900139000", "normal", 200, 2, 50, 30,
                "login_banned", "违规发帖", "secret", "广州", "440000,440100", "1990-1-1");
    }

    private void saveMember(String userNo, String nickname, String phone, String levelCode,
                            double consume, int orders, int points, int growth, String status,
                            String remark, String gender, String city, String cityCodes, String birthday) {
        MemberEntity m = new MemberEntity();
        m.setUserNo(userNo);
        m.setNickname(nickname);
        m.setPhone(phone);
        m.setAvatar(AVATAR);
        m.setLevelCode(levelCode);
        m.setConsumeAmount(BigDecimal.valueOf(consume));
        m.setOrderCount(orders);
        m.setPoints(points);
        m.setGrowthValue(growth);
        m.setStatus(status);
        m.setRemark(remark);
        m.setGender(gender);
        m.setCity(city);
        m.setCityCodes(cityCodes);
        m.setBirthday(birthday);
        m.setRegisterTime(LocalDateTime.of(2024, 8, 15, 14, 15, 13));
        m.setSource("APP");
        m.setPermissionsJson(Jsons.toJson(
                "login_banned".equals(status) ? List.of("login_banned") : List.of("normal")));
        m.setCouponCount(10);
        m.setReviewCount(20);
        m.setReturnCount(5);
        m.setLoginCount(10);
        m.setFavoriteProducts(5);
        m.setFavoriteTopics(10);
        m.setOrderFriends(100);
        m.setLotteryCount(10);
        m.setLastIp("114.55.25.01");
        memberRepository.save(m);
    }

    private void seedAddresses() {
        saveAddress("BZ6542", "李天霸", "13088880000",
                "广东省深圳市龙华区", "民治街道民乐新村100号", true);
        saveAddress("BZ6542", "李天霸", "13088880001",
                "广东省深圳市南山区", "科技园南区A栋", false);
    }

    private void saveAddress(String userNo, String name, String phone, String region, String detail, boolean def) {
        MemberAddressEntity a = new MemberAddressEntity();
        a.setUserNo(userNo);
        a.setContactName(name);
        a.setPhone(phone);
        a.setRegion(region);
        a.setDetailAddress(detail);
        a.setIsDefault(def);
        addressRepository.save(a);
    }

    private void seedTags() {
        MemberTagSaveRequest r1 = new MemberTagSaveRequest();
        r1.setName("优质用户");
        r1.setGender(List.of("all"));
        r1.setMemberLevels(List.of("all"));
        r1.setCityMode("all");
        r1.setOrderCountEnabled(true);
        r1.setOrderCountType("cumulative");
        r1.setOrderCount(10);
        r1.setAmountEnabled(true);
        r1.setAmountType("cumulative");
        r1.setAmount(2000.0);
        saveTag("BZ6542", r1);

        MemberTagSaveRequest r2 = new MemberTagSaveRequest();
        r2.setName("新用户");
        r2.setGender(List.of("all"));
        r2.setMemberLevels(List.of("all"));
        r2.setCityMode("all");
        r2.setOrderCountEnabled(true);
        r2.setOrderCountType("cumulative");
        r2.setOrderCount(0);
        saveTag("BZ6543", r2);

        MemberTagSaveRequest r3 = new MemberTagSaveRequest();
        r3.setName("复购用户");
        r3.setGender(List.of("all"));
        r3.setMemberLevels(List.of("all"));
        r3.setCityMode("all");
        r3.setOrderCountEnabled(true);
        r3.setOrderCountType("cumulative");
        r3.setOrderCount(5);
        r3.setAmountEnabled(true);
        r3.setAmountType("cumulative");
        r3.setAmount(1000.0);
        saveTag("BZ6544", r3);
    }

    private void saveTag(String tagCode, MemberTagSaveRequest rule) {
        rule.setId(tagCode);
        memberService.saveTag(rule);
    }

    private void seedTasks() {
        saveTask("sign_in", "每日签到", 5, 5, true, "每日签到奖励", 1);
        saveTask("order_pay", "完成下单", 10, 10, true, "订单支付完成", 2);
        saveTask("review", "商品评价", 5, 5, true, "发表商品评价", 3);
        saveTask("register", "注册有礼", 100, 100, true, "新用户注册", 4);
    }

    private void saveTask(String code, String name, int g, int p, boolean enabled, String desc, int sort) {
        GrowthTaskEntity t = new GrowthTaskEntity();
        t.setTaskCode(code);
        t.setTaskName(name);
        t.setGrowthReward(g);
        t.setPointsReward(p);
        t.setEnabled(enabled);
        t.setDescription(desc);
        t.setSortNum(sort);
        taskRepository.save(t);
    }
}
