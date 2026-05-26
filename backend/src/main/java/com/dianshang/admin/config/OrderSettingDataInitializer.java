package com.dianshang.admin.config;

import com.dianshang.admin.order.entity.ExpressTemplateEntity;
import com.dianshang.admin.order.entity.OrderAddressEntity;
import com.dianshang.admin.order.repository.ExpressTemplateRepository;
import com.dianshang.admin.order.repository.OrderAddressRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class OrderSettingDataInitializer implements CommandLineRunner {

    private final ExpressTemplateRepository expressTemplateRepository;
    private final OrderAddressRepository orderAddressRepository;

    public OrderSettingDataInitializer(ExpressTemplateRepository expressTemplateRepository,
                                     OrderAddressRepository orderAddressRepository) {
        this.expressTemplateRepository = expressTemplateRepository;
        this.orderAddressRepository = orderAddressRepository;
    }

    @Override
    public void run(String... args) {
        if (expressTemplateRepository.count() == 0) {
            seedExpressTemplates();
        }
        if (orderAddressRepository.count() == 0) {
            seedAddresses();
        }
    }

    private void seedExpressTemplates() {
        saveTemplate("ET001", "申通标准面单", "申通快递", "100*180mm", "默认申通模板", true, true, 10);
        saveTemplate("ET002", "顺丰热敏面单", "顺丰速运", "76*130mm", "", false, true, 9);
        saveTemplate("ET003", "圆通一联单", "圆通速递", "100*180mm", "", false, true, 8);
    }

    private void saveTemplate(String code, String name, String company, String spec, String remark,
                              boolean isDefault, boolean visible, int sort) {
        ExpressTemplateEntity e = new ExpressTemplateEntity();
        e.setTemplateCode(code);
        e.setTemplateName(name);
        e.setExpressCompany(company);
        e.setTemplateSpec(spec);
        e.setRemark(remark);
        e.setIsDefault(isDefault);
        e.setVisible(visible);
        e.setSortNum(sort);
        e.setAddTime(LocalDateTime.of(2024, 8, 6, 17, 8, 56));
        expressTemplateRepository.save(e);
    }

    private void seedAddresses() {
        saveAddress("AD001", "ship", "暴走仓库", "0755-88886666",
                "440000", "440300", "440307", "广东省", "深圳市", "龙岗区",
                "民治街道民乐新村仓储中心1号库", "518000", true, true, 10);
        saveAddress("AD002", "ship", "华南分仓", "0755-88887777",
                "440000", "440100", "440106", "广东省", "广州市", "天河区",
                "科韵路软件园B座", "510000", false, true, 9);
        saveAddress("AD003", "return", "售后收货组", "0755-88889999",
                "440000", "440300", "440305", "广东省", "深圳市", "南山区",
                "科技园退货中心A栋", "518057", true, true, 10);
        saveAddress("AD004", "return", "华东退货点", "021-66668888",
                "330000", "330100", "330106", "浙江省", "杭州市", "西湖区",
                "张江高科技园区祖冲之路", "310000", false, true, 8);
    }

    private void saveAddress(String code, String type, String contact, String phone,
                             String provinceCode, String cityCode, String districtCode,
                             String province, String city, String district, String detail,
                             String zip, boolean isDefault, boolean visible, int sort) {
        OrderAddressEntity e = new OrderAddressEntity();
        e.setAddressCode(code);
        e.setAddressType(type);
        e.setContactName(contact);
        e.setPhone(phone);
        e.setProvinceCode(provinceCode);
        e.setCityCode(cityCode);
        e.setDistrictCode(districtCode);
        e.setProvince(province);
        e.setCity(city);
        e.setDistrict(district);
        e.setDetailAddress(detail);
        e.setZipCode(zip);
        e.setIsDefault(isDefault);
        e.setVisible(visible);
        e.setSortNum(sort);
        e.setAddTime(LocalDateTime.of(2024, 8, 6, 17, 8, 56));
        orderAddressRepository.save(e);
    }
}
