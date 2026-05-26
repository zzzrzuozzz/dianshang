package com.dianshang.admin.ops.service;

import com.dianshang.admin.common.BusinessException;
import com.dianshang.admin.ops.dto.ExpressPreviewVO;
import com.dianshang.admin.ops.dto.ExpressSkuItemVO;
import com.dianshang.admin.order.entity.ExpressTemplateEntity;
import com.dianshang.admin.order.entity.OrderAddressEntity;
import com.dianshang.admin.order.entity.OrderEntity;
import com.dianshang.admin.order.repository.OrderRepository;
import com.dianshang.admin.order.service.ExpressTemplateService;
import com.dianshang.admin.order.service.OrderAddressService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ExpressService {

    private final OrderRepository orderRepository;
    private final OrderAddressService orderAddressService;
    private final ExpressTemplateService expressTemplateService;

    public ExpressService(OrderRepository orderRepository,
                          OrderAddressService orderAddressService,
                          ExpressTemplateService expressTemplateService) {
        this.orderRepository = orderRepository;
        this.orderAddressService = orderAddressService;
        this.expressTemplateService = expressTemplateService;
    }

    public ExpressPreviewVO preview(String orderId) {
        OrderEntity order = orderRepository.findByOrderNoAndDeletedFalse(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));

        ExpressTemplateEntity template = expressTemplateService.findDefault();
        OrderAddressEntity shipAddress = orderAddressService.findDefaultShipEntity();

        ExpressPreviewVO vo = new ExpressPreviewVO();
        vo.setOrderId(order.getOrderNo());
        vo.setReceiverName(order.getReceiverName());
        vo.setReceiverPhone(order.getReceiverPhone());
        vo.setReceiverAddress(nullToEmpty(order.getReceiverAddress()));

        if (shipAddress != null) {
            vo.setSenderName(shipAddress.getContactName());
            vo.setSenderPhone(shipAddress.getPhone());
            vo.setSenderAddress(buildFullAddress(shipAddress));
        } else {
            vo.setSenderName("暴走电商仓");
            vo.setSenderPhone("400-888-8888");
            vo.setSenderAddress("广东省深圳市龙华区民治街道仓储中心");
        }

        if (template != null) {
            vo.setExpressCompany(template.getExpressCompany());
            vo.setTemplateName(template.getTemplateName());
            vo.setTemplateSpec(template.getTemplateSpec());
        } else if (StringUtils.hasText(order.getLogistics())) {
            vo.setExpressCompany(extractCompany(order.getLogistics()));
        } else {
            vo.setExpressCompany("申通快递");
        }

        String expressNo = order.getLogisticsNo();
        if (!StringUtils.hasText(expressNo) || "未发货".equals(expressNo)) {
            expressNo = "ST" + order.getOrderNo();
        }
        vo.setExpressNumber(expressNo);

        ExpressSkuItemVO sku = new ExpressSkuItemVO();
        sku.setName(order.getProductName());
        sku.setSpec(order.getSpec());
        sku.setQuantity(order.getQuantity());
        vo.setSkuList(List.of(sku));
        return vo;
    }

    private static String extractCompany(String logistics) {
        int idx = logistics.indexOf('(');
        if (idx > 0) {
            return logistics.substring(0, idx).trim();
        }
        int space = logistics.indexOf(' ');
        return space > 0 ? logistics.substring(0, space) : logistics;
    }

    private static String buildFullAddress(OrderAddressEntity e) {
        return Stream.of(e.getProvince(), e.getCity(), e.getDistrict(), e.getDetailAddress())
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(""));
    }

    private static String nullToEmpty(String v) {
        return v != null ? v : "";
    }
}
