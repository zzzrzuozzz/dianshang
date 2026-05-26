package com.dianshang.admin.config;

import com.dianshang.admin.system.entity.RegionEntity;
import com.dianshang.admin.system.repository.RegionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** 演示种子；完整国标由 region/region-full.sql 导入，仅表为空时生效。 */
@Component
@Order(30)
public class RegionDataInitializer implements CommandLineRunner {

    private final RegionRepository regionRepository;

    public RegionDataInitializer(RegionRepository regionRepository) {
        this.regionRepository = regionRepository;
    }

    @Override
    public void run(String... args) {
        if (regionRepository.count() > 0) {
            return;
        }
        seed();
    }

    private void seed() {
        save("110000", "北京市", "0", 1);
        save("110100", "北京市", "110000", 2);
        save("110101", "东城区", "110100", 3);
        save("110105", "朝阳区", "110100", 3);
        save("110108", "海淀区", "110100", 3);

        save("330000", "浙江省", "0", 1);
        save("330100", "杭州市", "330000", 2);
        save("330102", "上城区", "330100", 3);
        save("330106", "西湖区", "330100", 3);
        save("330108", "滨江区", "330100", 3);
        save("330200", "宁波市", "330000", 2);
        save("330203", "海曙区", "330200", 3);
        save("330212", "鄞州区", "330200", 3);

        save("320000", "江苏省", "0", 1);
        save("320100", "南京市", "320000", 2);
        save("320102", "玄武区", "320100", 3);
        save("320104", "秦淮区", "320100", 3);
        save("320114", "雨花台区", "320100", 3);

        save("440000", "广东省", "0", 1);
        save("440100", "广州市", "440000", 2);
        save("440103", "荔湾区", "440100", 3);
        save("440106", "天河区", "440100", 3);
        save("440300", "深圳市", "440000", 2);
        save("440303", "罗湖区", "440300", 3);
        save("440305", "南山区", "440300", 3);
        save("440306", "宝安区", "440300", 3);
        save("440307", "龙岗区", "440300", 3);
    }

    private void save(String code, String name, String parent, int level) {
        RegionEntity e = new RegionEntity();
        e.setCode(code);
        e.setName(name);
        e.setParentCode(parent);
        e.setLevelNum(level);
        regionRepository.save(e);
    }
}
