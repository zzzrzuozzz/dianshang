package com.dianshang.admin.config;

import com.dianshang.admin.order.entity.AfterSaleEntity;
import com.dianshang.admin.order.entity.OrderEntity;
import com.dianshang.admin.order.entity.ReturnReasonEntity;
import com.dianshang.admin.order.repository.AfterSaleRepository;
import com.dianshang.admin.order.repository.OrderRepository;
import com.dianshang.admin.order.repository.ReturnReasonRepository;
import com.dianshang.admin.product.entity.Product;
import com.dianshang.admin.product.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class OrderDataInitializer implements CommandLineRunner {

    private static final String THUMB =
            "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png";

    private final OrderRepository orderRepository;
    private final AfterSaleRepository afterSaleRepository;
    private final ReturnReasonRepository returnReasonRepository;
    private final ProductRepository productRepository;

    public OrderDataInitializer(OrderRepository orderRepository,
                                AfterSaleRepository afterSaleRepository,
                                ReturnReasonRepository returnReasonRepository,
                                ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.afterSaleRepository = afterSaleRepository;
        this.returnReasonRepository = returnReasonRepository;
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        if (orderRepository.count() > 0) {
            return;
        }
        seedOrders();
        seedAfterSales();
        seedReturnReasons();
    }

    private void seedOrders() {
        saveOrder("BZ6542", "100角磨机配件手动自锁压板神箭手", "莫兰迪双色 (蓝色)", 1,
                "56", "-36", "8", false, "pending_ship", "not_shipped", "",
                "2024-08-06 17:08:56", "2024-08-16 17:08:56",
                "李大海", "13088880000", "广东省深圳市龙华区民治街道民乐新村", "自营");
        saveOrder("BZ6543", "2024新款夏季纯棉T恤 男女同款", "白色 XL", 2,
                "72", "-20", "0", true, "shipped", "in_transit", "申通快递 (st26546464)",
                "2024-08-05 14:20:30", "2024-08-15 14:20:30",
                "王小明", "13800138000", "北京市朝阳区望京街道阜通东大街", "第三方");
        saveOrder("BZ6544", "进口全脂纯牛奶 1L*12盒整箱装", "整箱装", 1,
                "69", "-20", "0", true, "pending_payment", "none", "",
                null, null,
                "张三", "13900139000", "上海市浦东新区陆家嘴环路1000号", "自营");

        for (int i = 5; i <= 15; i++) {
            String no = "BZ654" + i;
            String status = i % 4 == 0 ? "pending_payment" : (i % 3 == 0 ? "shipped" : "pending_ship");
            String ship = "shipped".equals(status) ? "in_transit" : ("pending_payment".equals(status) ? "none" : "not_shipped");
            saveOrder(no, "示例订单商品 " + no, "默认规格", 1,
                    "99", "-10", "8", false, status, ship,
                    "shipped".equals(status) ? "圆通快递 (YT" + no + ")" : "",
                    status.equals("pending_payment") ? null : "2024-08-0" + (i % 9 + 1) + " 10:00:00",
                    "shipped".equals(status) ? "2024-08-2" + (i % 9) + " 10:00:00" : null,
                    "用户" + i, "138000000" + i, "广东省广州市天河区示例路" + i + "号", i % 2 == 0 ? "自营" : "第三方");
        }
    }

    private void saveOrder(String orderNo, String productName, String spec, int qty,
                           String actual, String discount, String freight, boolean freightFree,
                           String orderStatus, String shipStatus, String logistics,
                           String payTime, String autoConfirm,
                           String receiverName, String phone, String address, String supplier) {
        OrderEntity o = new OrderEntity();
        o.setOrderNo(orderNo);
        o.setProductName(productName);
        o.setThumb(THUMB);
        o.setSpec(spec);
        o.setQuantity(qty);
        BigDecimal actualAmount = new BigDecimal(actual);
        o.setActualAmount(actualAmount);
        o.setDiscountAmount(new BigDecimal(discount));
        o.setFreight(new BigDecimal(freight));
        o.setFreightFree(freightFree);
        o.setOrderStatus(orderStatus);
        o.setShipStatus(shipStatus);
        o.setLogistics(logistics);
        o.setPayTime(parseTime(payTime));
        o.setAutoConfirmTime(parseTime(autoConfirm));
        o.setCreateTime(LocalDateTime.of(2024, 8, 2, 16, 29));
        o.setReceiverName(receiverName);
        o.setReceiverPhone(phone);
        o.setReceiverAddress(address);
        o.setSupplier(supplier);
        o.setSupplierPhone("自营".equals(supplier) ? "400-888-8888" : "010-12345678");
        o.setUserAccount(phone + " " + receiverName);
        o.setPayMethod("pending_payment".equals(orderStatus) ? "未支付" : "支付宝");
        o.setPayMethodDetail("pending_payment".equals(orderStatus) ? "" : "2088123456789012");
        o.setOrderSource("APP");
        o.setOrderType("普通订单");
        o.setDeliveryMethod("快递");
        o.setLogisticsNo("shipped".equals(orderStatus) ? "ST" + orderNo : "未发货");
        o.setDeliverySerial("shipped".equals(orderStatus) ? "FH20240802001" : "未发货");
        o.setAutoConfirmDaysLabel("确定收货后15天");
        o.setUserRemark("快速发货，急用");
        o.setPlatformRemark("");
        o.setInvoiceType("电子发票");
        o.setInvoiceStatus("pending_payment".equals(orderStatus) ? "未开票" : "已开票");
        o.setInvoiceAttr("个人");
        o.setInvoiceTitle(receiverName);
        o.setInvoiceTaxNo("");
        o.setInvoiceContent("服装");
        o.setInvoiceEmail("user@example.com");
        o.setProductSku("SKU-" + orderNo);
        o.setUnitPrice(actualAmount);
        o.setProductTotal(actualAmount.add(new BigDecimal("144")));
        o.setCouponAmount(new BigDecimal(discount).abs());
        o.setPayableSubtotal(actualAmount.add(new BigDecimal(freight)));
        o.setAfterSalesStatus("none");
        o.setProductNo(resolveProductNo(productName));
        orderRepository.save(o);
    }

    private String resolveProductNo(String productName) {
        return productRepository.findAll((root, q, cb) -> cb.and(
                cb.isFalse(root.get("deleted")),
                cb.equal(root.get("title"), productName)
        )).stream().findFirst().map(Product::getProductNo).orElse(null);
    }

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-M-d HH:mm");

    private LocalDateTime parseTime(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(text, TIME_FMT);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    private void seedAfterSales() {
        saveAfterSale("AS6542", "BZ6542", "100角磨机配件手动自锁压板神箭手",
                "paid", "not_shipped", "platform_pending", "refund_only", "8.0",
                "2024-08-03 15:40", "2024-08-03 15:40");
        saveAfterSale("AS6543", "BZ6543", "2024新款夏季纯棉T恤 男女同款",
                "shipped", "in_transit", "user_pending", "return_refund", "72.0",
                "2024-08-04 10:20", null);
        saveAfterSale("AS6544", "BZ6544", "进口全脂纯牛奶 1L*12盒整箱装",
                "completed", "signed", "completed", "exchange", "0.0",
                "2024-08-01 09:00", "2024-08-02 16:30");
    }

    private void saveAfterSale(String no, String orderNo, String productName,
                               String orderStatus, String shipStatus, String afterStatus, String type,
                               String refund, String apply, String process) {
        AfterSaleEntity a = new AfterSaleEntity();
        a.setAfterSaleNo(no);
        a.setOrderNo(orderNo);
        a.setProductName(productName);
        a.setThumb(THUMB);
        a.setOrderStatus(orderStatus);
        a.setShipStatus(shipStatus);
        a.setAfterSaleStatus(afterStatus);
        a.setAfterSaleType(type);
        a.setRefundAmount(new BigDecimal(refund));
        a.setApplyTime(parseTime(apply));
        a.setProcessTime(parseTime(process));
        afterSaleRepository.save(a);
    }

    private void seedReturnReasons() {
        saveReason("BZ6542", "商品信息拍错 (规格/尺码/颜色等)", 10, true);
        saveReason("BZ6543", "七天无理由", 9, true);
        saveReason("BZ6544", "质量问题", 8, false);
        saveReason("BZ6545", "不喜欢/不想要", 7, true);
    }

    private void saveReason(String code, String text, int sort, boolean visible) {
        ReturnReasonEntity r = new ReturnReasonEntity();
        r.setReasonCode(code);
        r.setReasonText(text);
        r.setSortNum(sort);
        r.setVisible(visible);
        r.setAddTime(LocalDateTime.of(2024, 8, 6, 17, 8, 56));
        returnReasonRepository.save(r);
    }
}
