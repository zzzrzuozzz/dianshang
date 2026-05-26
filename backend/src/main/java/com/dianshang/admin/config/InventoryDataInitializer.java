package com.dianshang.admin.config;

import com.dianshang.admin.inventory.entity.InventorySkuEntity;
import com.dianshang.admin.inventory.entity.StockFlowEntity;
import com.dianshang.admin.inventory.repository.InventorySkuRepository;
import com.dianshang.admin.inventory.repository.StockFlowRepository;
import com.dianshang.admin.product.entity.Product;
import com.dianshang.admin.product.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class InventoryDataInitializer implements CommandLineRunner {

    private final StockFlowRepository stockFlowRepository;
    private final InventorySkuRepository inventorySkuRepository;
    private final ProductRepository productRepository;

    public InventoryDataInitializer(StockFlowRepository stockFlowRepository,
                                  InventorySkuRepository inventorySkuRepository,
                                  ProductRepository productRepository) {
        this.stockFlowRepository = stockFlowRepository;
        this.inventorySkuRepository = inventorySkuRepository;
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        if (stockFlowRepository.count() > 0) {
            return;
        }
        seedExtraSkus();
        seedSampleFlows();
        initProductWarnings();
    }

    private void seedExtraSkus() {
        productRepository.findByProductNoAndDeletedFalse("025342").ifPresent(p -> {
            if (inventorySkuRepository.findByProductNoAndSkuLineIdAndDeletedFalse("025342", "2656566").isEmpty()) {
                InventorySkuEntity sku = new InventorySkuEntity();
                sku.setProductNo("025342");
                sku.setSkuLineId("2656566");
                sku.setSkuName("莫兰迪双色 (红色)");
                sku.setSkuCode("sg12-58g-66");
                sku.setActualStock(800);
                sku.setWarningStock(500);
                sku.setWarehouseCode("WH-001");
                inventorySkuRepository.save(sku);
            }
        });
    }

    private void initProductWarnings() {
        productRepository.findAll().forEach(p -> {
            if (Boolean.TRUE.equals(p.getDeleted())) {
                return;
            }
            if (p.getStockWarning() == null || p.getStockWarning() == 0) {
                p.setStockWarning(Math.max(50, p.getStock() / 10));
            }
            if (p.getFrozenStock() == null) {
                p.setFrozenStock(p.getStock() > 100 ? 12 : 0);
            }
            productRepository.save(p);
        });
    }

    private void seedSampleFlows() {
        saveFlow("025342", "FL6542", "BZ6543", "BZ6543", "P-025342", "白色 XL",
                "sales_out", 1201, -1, 1200, "admin", "订单发货出库");
        saveFlow("025344", "FL6544", "PD20240805", null, "P-025344", "50只装",
                "manual_out", 100, -100, 0, "admin", "盘点调整出库");
        saveFlow("025342", "FL6545", "AS6542", "AS6542", "P-025342", "白色 XL",
                "return_in", 1199, 1, 1200, "admin", "退货入库");
        saveFlow("025343", "FL6546", "PO20240801", null, "P-025343", "整箱装",
                "manual_in", 800, 200, 1000, "warehouse01", "采购入库");
        saveFlow("025342", "FL6547", "BZ6543", "BZ6543", "P-025342", "白色 XL",
                "reissue", 1200, -1, 1199, "admin", "订单补发出库");
    }

    private void saveFlow(String productNo, String flowNo, String relatedNo, String orderId,
                        String skuLineId, String skuName, String type,
                        int before, int change, int after, String operator, String remark) {
        StockFlowEntity f = new StockFlowEntity();
        f.setFlowNo(flowNo);
        f.setProductNo(productNo);
        f.setRelatedNo(relatedNo);
        f.setOrderId(orderId);
        f.setSkuLineId(skuLineId);
        f.setSkuName(skuName);
        f.setFlowType(type);
        f.setBeforeQty(before);
        f.setChangeQty(change);
        f.setAfterQty(after);
        f.setOperatorName(operator);
        f.setRemark(remark);
        f.setCreatedAt(LocalDateTime.of(2024, 8, 8, 15, 9));
        stockFlowRepository.save(f);
    }
}
