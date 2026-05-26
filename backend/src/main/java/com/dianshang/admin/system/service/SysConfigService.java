package com.dianshang.admin.system.service;

import com.dianshang.admin.system.dto.PlatformConfigVO;
import com.dianshang.admin.system.dto.PlatformHintsVO;
import com.dianshang.admin.system.dto.SysConfigUpdateItem;
import com.dianshang.admin.system.dto.SysConfigVO;
import com.dianshang.admin.system.entity.SysConfig;
import com.dianshang.admin.system.repository.SysConfigRepository;
import com.dianshang.admin.system.support.SysConfigKeys;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SysConfigService {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysConfigRepository configRepository;
    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();

    public SysConfigService(SysConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    public List<SysConfigVO> listAll() {
        return configRepository.findAllByOrderByConfigKeyAsc().stream()
                .map(this::toVO)
                .toList();
    }

    public Map<String, String> publicMap() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put(SysConfigKeys.SHOP_NAME, getValue(SysConfigKeys.SHOP_NAME, "暴走电商"));
        map.put(SysConfigKeys.SERVICE_PHONE, getValue(SysConfigKeys.SERVICE_PHONE, "400-888-8888"));
        map.put(SysConfigKeys.FREE_SHIP_THRESHOLD, getValue(SysConfigKeys.FREE_SHIP_THRESHOLD, "99"));
        map.put(SysConfigKeys.STOCK_DEDUCT_STRATEGY, getValue(SysConfigKeys.STOCK_DEDUCT_STRATEGY, "order"));
        return map;
    }

    public PlatformConfigVO getPlatformConfig() {
        PlatformConfigVO vo = new PlatformConfigVO();
        vo.setShopName(getValue(SysConfigKeys.SHOP_NAME, "暴走电商"));
        vo.setServicePhone(getValue(SysConfigKeys.SERVICE_PHONE, "400-888-8888"));
        vo.setFreeShipThreshold(getFreeShipThreshold());
        vo.setUnpaidCloseMinutes(getUnpaidCloseMinutes());
        vo.setStockDeductStrategy(getValue(SysConfigKeys.STOCK_DEDUCT_STRATEGY, "order"));
        vo.setStockDeductStrategyLabel(stockDeductLabel(vo.getStockDeductStrategy()));
        configRepository.findAllByOrderByConfigKeyAsc().stream()
                .map(SysConfig::getUpdateTime)
                .filter(t -> t != null)
                .max(java.time.LocalDateTime::compareTo)
                .ifPresent(t -> vo.setLastUpdateTime(t.format(DT_FMT)));
        return vo;
    }

    @Transactional
    public void savePlatformConfig(PlatformConfigVO vo) {
        List<SysConfigUpdateItem> items = new ArrayList<>();
        items.add(item(SysConfigKeys.SHOP_NAME, vo.getShopName(), "商城品牌名称"));
        items.add(item(SysConfigKeys.SERVICE_PHONE, vo.getServicePhone(), "官方客服热线"));
        items.add(item(SysConfigKeys.FREE_SHIP_THRESHOLD,
                vo.getFreeShipThreshold() != null ? vo.getFreeShipThreshold().toPlainString() : "0",
                "满额包邮门槛(元)"));
        items.add(item(SysConfigKeys.UNPAID_CLOSE_MINUTES,
                vo.getUnpaidCloseMinutes() != null ? String.valueOf(vo.getUnpaidCloseMinutes()) : "30",
                "未付款订单自动关闭(分钟)"));
        items.add(item(SysConfigKeys.STOCK_DEDUCT_STRATEGY, vo.getStockDeductStrategy(), "库存扣减策略: order|pay"));
        updateBatch(items);
    }

    public PlatformHintsVO buildPlatformHints() {
        PlatformHintsVO h = new PlatformHintsVO();
        h.setShopName(getValue(SysConfigKeys.SHOP_NAME, "暴走电商"));
        h.setServicePhone(getValue(SysConfigKeys.SERVICE_PHONE, "400-888-8888"));
        BigDecimal threshold = getFreeShipThreshold();
        h.setFreeShipRuleText("满 " + threshold.stripTrailingZeros().toPlainString() + " 元包邮");
        h.setUnpaidCloseRuleText("未付款订单 " + getUnpaidCloseMinutes() + " 分钟后自动关闭");
        h.setStockDeductLabel(stockDeductLabel(getValue(SysConfigKeys.STOCK_DEDUCT_STRATEGY, "order")));
        return h;
    }

    public boolean isStockDeductOnPay() {
        return "pay".equalsIgnoreCase(getValue(SysConfigKeys.STOCK_DEDUCT_STRATEGY, "order"));
    }

    public BigDecimal getFreeShipThreshold() {
        try {
            return new BigDecimal(getValue(SysConfigKeys.FREE_SHIP_THRESHOLD, "99"));
        } catch (NumberFormatException e) {
            return new BigDecimal("99");
        }
    }

    public int getUnpaidCloseMinutes() {
        try {
            return Integer.parseInt(getValue(SysConfigKeys.UNPAID_CLOSE_MINUTES, "30"));
        } catch (NumberFormatException e) {
            return 30;
        }
    }

    public String getValue(String key, String defaultValue) {
        String cached = cache.get(key);
        if (cached != null) {
            return cached;
        }
        String value = configRepository.findByConfigKey(key)
                .map(SysConfig::getConfigValue)
                .orElse(defaultValue);
        cache.put(key, value);
        return value;
    }

    private SysConfigUpdateItem item(String key, String value, String remark) {
        SysConfigUpdateItem i = new SysConfigUpdateItem();
        i.setConfigKey(key);
        i.setConfigValue(value != null ? value.trim() : "");
        i.setRemark(remark);
        return i;
    }

    private String stockDeductLabel(String strategy) {
        return "pay".equalsIgnoreCase(strategy) ? "付款减库存" : "下单减库存";
    }

    @Transactional
    public void updateBatch(List<SysConfigUpdateItem> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        for (SysConfigUpdateItem item : items) {
            if (!StringUtils.hasText(item.getConfigKey())) {
                continue;
            }
            String key = item.getConfigKey().trim();
            SysConfig config = configRepository.findByConfigKey(key).orElseGet(() -> {
                SysConfig c = new SysConfig();
                c.setConfigKey(key);
                return c;
            });
            config.setConfigValue(item.getConfigValue() != null ? item.getConfigValue().trim() : "");
            if (StringUtils.hasText(item.getRemark())) {
                config.setRemark(item.getRemark().trim());
            }
            configRepository.save(config);
            cache.put(key, config.getConfigValue());
        }
    }

    public void clearCache() {
        cache.clear();
    }

    @Transactional
    public void ensureDefault(String key, String value, String remark) {
        if (configRepository.findByConfigKey(key).isEmpty()) {
            SysConfig c = new SysConfig();
            c.setConfigKey(key);
            c.setConfigValue(value);
            c.setRemark(remark);
            configRepository.save(c);
        }
    }

    private SysConfigVO toVO(SysConfig c) {
        SysConfigVO vo = new SysConfigVO();
        vo.setId(c.getId());
        vo.setConfigKey(c.getConfigKey());
        vo.setConfigValue(c.getConfigValue());
        vo.setRemark(c.getRemark());
        vo.setUpdateTime(c.getUpdateTime() != null ? c.getUpdateTime().format(DT_FMT) : null);
        return vo;
    }
}
