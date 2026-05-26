package com.dianshang.admin.inventory.service;

import com.dianshang.admin.common.BusinessException;
import com.dianshang.admin.common.TabCountVO;
import com.dianshang.admin.inventory.dto.*;
import com.dianshang.admin.inventory.entity.InventorySkuEntity;
import com.dianshang.admin.inventory.entity.StockFlowEntity;
import com.dianshang.admin.inventory.repository.InventorySkuRepository;
import com.dianshang.admin.inventory.repository.StockFlowRepository;
import com.dianshang.admin.inventory.support.InventorySpecifications;
import com.dianshang.admin.inventory.support.SecurityOperator;
import com.dianshang.admin.inventory.support.StockFlowSpecifications;
import com.dianshang.admin.order.entity.OrderEntity;
import com.dianshang.admin.system.service.SysConfigService;
import com.dianshang.admin.product.entity.Product;
import com.dianshang.admin.product.entity.ProductBrand;
import com.dianshang.admin.product.entity.ProductCategory;
import com.dianshang.admin.product.repository.ProductBrandRepository;
import com.dianshang.admin.product.repository.ProductCategoryRepository;
import com.dianshang.admin.product.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private static final DateTimeFormatter FLOW_TIME = DateTimeFormatter.ofPattern("yyyy-M-d HH:mm");
    private static final String PRIMARY_PREFIX = "P-";

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final ProductBrandRepository brandRepository;
    private final InventorySkuRepository inventorySkuRepository;
    private final StockFlowRepository stockFlowRepository;
    private final SecurityOperator securityOperator;
    private final SysConfigService sysConfigService;

    public InventoryService(ProductRepository productRepository,
                          ProductCategoryRepository categoryRepository,
                          ProductBrandRepository brandRepository,
                          InventorySkuRepository inventorySkuRepository,
                          StockFlowRepository stockFlowRepository,
                          SecurityOperator securityOperator,
                          SysConfigService sysConfigService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.inventorySkuRepository = inventorySkuRepository;
        this.stockFlowRepository = stockFlowRepository;
        this.securityOperator = securityOperator;
        this.sysConfigService = sysConfigService;
    }

    public InventoryPageVO list(String keyword, String categoryCode, String supplier,
                                String stockStatus, String tab, int page, int pageSize) {
        String resolvedCategory = resolveCategoryCode(categoryCode);
        Page<Product> result = productRepository.findAll(
                InventorySpecifications.forInventoryList(keyword, resolvedCategory, supplier, stockStatus, tab),
                PageRequest.of(Math.max(page - 1, 0), pageSize, Sort.by(Sort.Direction.DESC, "sortNum", "id")));

        Map<String, String> categoryNames = loadCategoryNames();
        Map<String, String> brandNames = loadBrandNames();

        InventoryPageVO vo = new InventoryPageVO();
        vo.setList(result.getContent().stream()
                .map(p -> toListVO(p, categoryNames, brandNames))
                .toList());
        vo.setTotal(result.getTotalElements());
        vo.setPage(page);
        vo.setPageSize(pageSize);
        vo.setTotalPages((int) Math.ceil((double) result.getTotalElements() / pageSize));
        vo.setTabs(buildInventoryTabs());
        return vo;
    }

    @Transactional
    public void updateStock(InventoryUpdateRequest request) {
        Product product = productRepository.findByProductNoAndDeletedFalse(request.getGoodsId())
                .orElseThrow(() -> new BusinessException("商品不存在"));

        String operator = securityOperator.currentUsername();
        for (InventorySkuVO sku : request.getSkus()) {
            if (isPrimarySku(sku.getSkuId(), product)) {
                int before = product.getStock();
                int after = sku.getActualStock() != null ? sku.getActualStock() : before;
                applyStockChange(product, product.getSku(), product.getSubtitle(), before, after,
                        "手动调整主规格库存", operator);
                product.setStock(after);
                product.setStockWarning(sku.getWarningStock() != null ? sku.getWarningStock() : product.getStockWarning());
                productRepository.save(product);
            } else {
                InventorySkuEntity line = inventorySkuRepository
                        .findByProductNoAndSkuLineIdAndDeletedFalse(product.getProductNo(), sku.getSkuId())
                        .orElseGet(() -> {
                            InventorySkuEntity e = new InventorySkuEntity();
                            e.setProductNo(product.getProductNo());
                            e.setSkuLineId(sku.getSkuId());
                            return e;
                        });
                int before = line.getId() == null ? 0 : line.getActualStock();
                int after = sku.getActualStock() != null ? sku.getActualStock() : before;
                if (line.getId() != null && !Objects.equals(before, after)) {
                    recordFlow(product, line.getSkuLineId(), line.getSkuName(), before, after - before,
                            after, "manual_in", null, null,
                            "手动调整扩展 SKU 库存", operator);
                } else if (line.getId() == null && after > 0) {
                    recordFlow(product, sku.getSkuId(), sku.getSkuName(), 0, after, after,
                            "manual_in", null, null, "初始化扩展 SKU 库存", operator);
                }
                line.setSkuName(sku.getSkuName());
                line.setSkuCode(sku.getSkuCode());
                line.setActualStock(after);
                line.setWarningStock(sku.getWarningStock() != null ? sku.getWarningStock() : 0);
                line.setWarehouseCode(StringUtils.hasText(sku.getWarehouseCode()) ? sku.getWarehouseCode() : "WH-001");
                line.setDeleted(false);
                inventorySkuRepository.save(line);
            }
        }
    }

    public StockFlowPageVO flowList(String flowNo, String productKeyword, String bizType, String flowTab,
                                    String goodsId, LocalDate start, LocalDate end, int page, int pageSize) {
        Specification<StockFlowEntity> spec = StockFlowSpecifications.forFlowList(
                flowNo, productKeyword, bizType, flowTab, goodsId, start, end);

        Page<StockFlowEntity> result = stockFlowRepository.findAll(spec,
                PageRequest.of(Math.max(page - 1, 0), pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));

        Set<String> productNos = result.getContent().stream()
                .map(StockFlowEntity::getProductNo)
                .collect(Collectors.toSet());
        Map<String, Product> productMap = productRepository.findAll(
                (root, q, cb) -> root.get("productNo").in(productNos.isEmpty() ? List.of("__none__") : productNos))
                .stream()
                .collect(Collectors.toMap(Product::getProductNo, p -> p, (a, b) -> a));

        List<StockFlowEntity> filtered = result.getContent();
        if (StringUtils.hasText(productKeyword)) {
            String kw = productKeyword.trim().toLowerCase();
            filtered = filtered.stream()
                    .filter(f -> {
                        Product p = productMap.get(f.getProductNo());
                        return p != null && p.getTitle().toLowerCase().contains(kw);
                    })
                    .toList();
        }

        StockFlowPageVO vo = new StockFlowPageVO();
        vo.setList(filtered.stream().map(f -> toFlowVO(f, productMap.get(f.getProductNo()))).toList());
        vo.setTotal(result.getTotalElements());
        vo.setPage(page);
        vo.setPageSize(pageSize);
        vo.setTotalPages((int) Math.ceil((double) result.getTotalElements() / pageSize));
        vo.setTabs(buildFlowTabs());
        return vo;
    }

    public byte[] exportFlowCsv(String flowNo, String productKeyword, String bizType, String flowTab,
                                String goodsId, LocalDate start, LocalDate end) {
        StockFlowPageVO page = flowList(flowNo, productKeyword, bizType, flowTab, goodsId, start, end, 1, 10000);
        StringBuilder sb = new StringBuilder();
        sb.append('\ufeff');
        sb.append("流水编号,相关单号,商品名称,SKU规格,变动类型,变动前,变动数量,变动后,操作人,操作时间,备注\n");
        Map<String, String> typeLabels = flowTypeLabels();
        for (StockFlowVO row : page.getList()) {
            sb.append(csv(row.getId())).append(',')
                    .append(csv(row.getRelatedNo())).append(',')
                    .append(csv(row.getName())).append(',')
                    .append(csv(row.getSkuName())).append(',')
                    .append(csv(typeLabels.getOrDefault(row.getType(), row.getType()))).append(',')
                    .append(row.getBeforeQty()).append(',')
                    .append(row.getChangeQty()).append(',')
                    .append(row.getAfterQty()).append(',')
                    .append(csv(row.getOperator())).append(',')
                    .append(csv(row.getTime())).append(',')
                    .append(csv(row.getRemark())).append('\n');
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 按平台配置的库存扣减策略出库（幂等：同一订单仅扣减一次 sales_out）。
     * order：发货时扣减；pay：已付款（有 payTime）后发货时扣减。
     */
    @Transactional
    public void deductStockForOrder(OrderEntity order, String scene) {
        if (order == null || !StringUtils.hasText(order.getOrderNo())) {
            return;
        }
        if (stockFlowRepository.existsByOrderIdAndFlowType(order.getOrderNo(), "sales_out")) {
            return;
        }
        boolean onPay = sysConfigService.isStockDeductOnPay();
        if (onPay) {
            if (!"pay".equals(scene) && !"ship".equals(scene)) {
                return;
            }
            if (order.getPayTime() == null && !"pending_ship".equals(order.getOrderStatus())
                    && !"paid".equals(order.getOrderStatus())) {
                return;
            }
        } else if (!"ship".equals(scene) && !"order".equals(scene)) {
            return;
        }
        shipOutbound(order);
    }

    @Transactional
    public void shipOutbound(OrderEntity order) {
        if (!StringUtils.hasText(order.getProductNo())) {
            return;
        }
        if (stockFlowRepository.existsByOrderIdAndFlowType(order.getOrderNo(), "sales_out")) {
            return;
        }
        Product product = productRepository.findByProductNoAndDeletedFalse(order.getProductNo()).orElse(null);
        if (product == null) {
            return;
        }
        int qty = order.getQuantity() != null ? order.getQuantity() : 1;
        int before = product.getStock();
        int after = Math.max(0, before - qty);
        product.setStock(after);
        productRepository.save(product);
        recordFlow(product, primarySkuId(product), product.getSubtitle(), before, -qty, after,
                "sales_out", order.getOrderNo(), order.getOrderNo(), "订单发货出库",
                securityOperator.currentUsername());
    }

    @Transactional
    public void reissueOutbound(OrderEntity order) {
        if (!StringUtils.hasText(order.getProductNo())) {
            throw new BusinessException("订单未关联商品，无法补发出库");
        }
        Product product = productRepository.findByProductNoAndDeletedFalse(order.getProductNo())
                .orElseThrow(() -> new BusinessException("关联商品不存在或已删除"));
        int qty = order.getQuantity() != null ? order.getQuantity() : 1;
        if (product.getStock() < qty) {
            throw new BusinessException("库存不足，无法补发（当前库存 " + product.getStock() + "）");
        }
        int before = product.getStock();
        int after = before - qty;
        product.setStock(after);
        productRepository.save(product);
        recordFlow(product, primarySkuId(product), product.getSubtitle(), before, -qty, after,
                "reissue", order.getOrderNo(), order.getOrderNo(), "订单补发出库",
                securityOperator.currentUsername());
    }

    @Transactional
    public void returnInbound(OrderEntity order, int qty) {
        if (!StringUtils.hasText(order.getProductNo()) || qty <= 0) {
            return;
        }
        Product product = productRepository.findByProductNoAndDeletedFalse(order.getProductNo()).orElse(null);
        if (product == null) {
            return;
        }
        int before = product.getStock();
        int after = before + qty;
        product.setStock(after);
        productRepository.save(product);
        recordFlow(product, primarySkuId(product), product.getSubtitle(), before, qty, after,
                "return_in", order.getOrderNo(), order.getOrderNo(), "退货入库",
                securityOperator.currentUsername());
    }

    private void applyStockChange(Product product, String skuLineId, String skuName,
                                  int before, int after, String remark, String operator) {
        if (before == after) {
            return;
        }
        int delta = after - before;
        String type = delta > 0 ? "manual_in" : "manual_out";
        recordFlow(product, skuLineId, skuName, before, delta, after, type, null, null, remark, operator);
    }

    private void recordFlow(Product product, String skuLineId, String skuName,
                          int before, int change, int after, String flowType,
                          String relatedNo, String orderId, String remark, String operator) {
        StockFlowEntity flow = new StockFlowEntity();
        flow.setFlowNo("FL" + System.currentTimeMillis() % 1000000);
        flow.setProductNo(product.getProductNo());
        flow.setRelatedNo(relatedNo);
        flow.setOrderId(orderId);
        flow.setSkuLineId(skuLineId);
        flow.setSkuName(skuName);
        flow.setFlowType(flowType);
        flow.setBeforeQty(before);
        flow.setChangeQty(change);
        flow.setAfterQty(after);
        flow.setOperatorName(operator);
        flow.setRemark(remark);
        stockFlowRepository.save(flow);
    }

    private InventoryListVO toListVO(Product p, Map<String, String> categoryNames, Map<String, String> brandNames) {
        InventoryListVO vo = new InventoryListVO();
        vo.setId(p.getProductNo());
        vo.setGoodsId(p.getProductNo());
        vo.setThumb(p.getThumb());
        vo.setName(p.getTitle());
        vo.setSkuName(p.getSubtitle());
        vo.setSkuId(primarySkuId(p));
        vo.setCategory(categoryNames.getOrDefault(p.getCategoryCode(), p.getCategoryCode()));
        vo.setSupplier(p.getSupplier());
        int frozen = p.getFrozenStock() != null ? p.getFrozenStock() : 0;
        vo.setActualStock(p.getStock());
        vo.setWarningStock(p.getStockWarning() != null ? p.getStockWarning() : 0);
        vo.setFrozenStock(frozen);
        vo.setAvailableStock(Math.max(0, p.getStock() - frozen));
        vo.setStatus(resolveStockStatus(p));
        vo.setBrand(brandNames.getOrDefault(p.getBrandCode(), p.getBrandCode()));
        vo.setSkus(buildSkuList(p));
        vo.setExpandTip(buildExpandTip(vo.getSkus()));
        return vo;
    }

    private List<InventorySkuVO> buildSkuList(Product p) {
        List<InventorySkuVO> list = new ArrayList<>();
        list.add(primarySkuVo(p));
        inventorySkuRepository.findByProductNoAndDeletedFalseOrderByIdAsc(p.getProductNo())
                .forEach(line -> list.add(toSkuVo(line)));
        return list;
    }

    private InventorySkuVO primarySkuVo(Product p) {
        InventorySkuVO vo = new InventorySkuVO();
        vo.setSkuName(StringUtils.hasText(p.getSubtitle()) ? p.getSubtitle() : p.getTitle());
        vo.setSkuId(primarySkuId(p));
        vo.setSkuCode(p.getSku());
        vo.setActualStock(p.getStock());
        vo.setWarningStock(p.getStockWarning() != null ? p.getStockWarning() : 0);
        vo.setWarehouseCode("WH-001");
        return vo;
    }

    private InventorySkuVO toSkuVo(InventorySkuEntity line) {
        InventorySkuVO vo = new InventorySkuVO();
        vo.setSkuName(line.getSkuName());
        vo.setSkuId(line.getSkuLineId());
        vo.setSkuCode(line.getSkuCode());
        vo.setActualStock(line.getActualStock());
        vo.setWarningStock(line.getWarningStock());
        vo.setWarehouseCode(line.getWarehouseCode());
        return vo;
    }

    private StockFlowVO toFlowVO(StockFlowEntity f, Product p) {
        StockFlowVO vo = new StockFlowVO();
        vo.setId(f.getFlowNo());
        vo.setGoodsId(f.getProductNo());
        vo.setRelatedNo(f.getRelatedNo());
        vo.setOrderId(f.getOrderId());
        if (p != null) {
            vo.setThumb(p.getThumb());
            vo.setName(p.getTitle());
        }
        vo.setSkuName(f.getSkuName());
        vo.setSkuId(f.getSkuLineId());
        vo.setType(f.getFlowType());
        vo.setBeforeQty(f.getBeforeQty());
        vo.setChangeQty(f.getChangeQty());
        vo.setAfterQty(f.getAfterQty());
        vo.setOperator(f.getOperatorName());
        vo.setTime(f.getCreatedAt().format(FLOW_TIME));
        vo.setRemark(f.getRemark());
        return vo;
    }

    private String buildExpandTip(List<InventorySkuVO> skus) {
        if (skus.size() <= 1) {
            return "";
        }
        long low = skus.stream()
                .filter(s -> s.getActualStock() > 0 && s.getActualStock() <= s.getWarningStock())
                .count();
        if (low > 0) {
            return "展开 SKU > 该商品有 " + skus.size() + " 个 SKU（" + low + " 个 SKU 库存低于预警值，请及时补货）";
        }
        return "展开 SKU > 该商品有 " + skus.size() + " 个 SKU";
    }

    private String resolveStockStatus(Product p) {
        int stock = p.getStock();
        int warning = p.getStockWarning() != null ? p.getStockWarning() : 0;
        if (stock == 0) {
            return "out";
        }
        if (stock > 0 && stock <= warning) {
            return "warning";
        }
        return "sufficient";
    }

    private boolean isPrimarySku(String skuId, Product product) {
        return primarySkuId(product).equals(skuId);
    }

    private String primarySkuId(Product p) {
        return PRIMARY_PREFIX + p.getProductNo();
    }

    private List<TabCountVO> buildInventoryTabs() {
        long all = productRepository.count((root, q, cb) -> cb.isFalse(root.get("deleted")));
        long warning = productRepository.count(InventorySpecifications.forInventoryList(null, null, null, null, "warning"));
        long out = productRepository.count(InventorySpecifications.forInventoryList(null, null, null, null, "out"));
        return List.of(
                new TabCountVO("all", "全部库存", all),
                new TabCountVO("warning", "预警商品", warning),
                new TabCountVO("out", "已售罄商品", out)
        );
    }

    private List<TabCountVO> buildFlowTabs() {
        long all = stockFlowRepository.count();
        return List.of(
                new TabCountVO("all", "全部", all),
                new TabCountVO("sales_out", "发货", stockFlowRepository.countByFlowType("sales_out")),
                new TabCountVO("return_in", "退货", stockFlowRepository.countByFlowType("return_in")),
                new TabCountVO("manual_out", "手动出库", stockFlowRepository.countByFlowType("manual_out")),
                new TabCountVO("manual_in", "手动入库", stockFlowRepository.countByFlowType("manual_in")),
                new TabCountVO("reissue", "补发", stockFlowRepository.countByFlowType("reissue"))
        );
    }

    private Map<String, String> loadCategoryNames() {
        return categoryRepository.findByDeletedFalseOrderBySortNumAsc().stream()
                .collect(Collectors.toMap(ProductCategory::getCode, ProductCategory::getName, (a, b) -> a));
    }

    private Map<String, String> loadBrandNames() {
        return brandRepository.findAll().stream()
                .filter(b -> !Boolean.TRUE.equals(b.getDeleted()))
                .collect(Collectors.toMap(ProductBrand::getCode, ProductBrand::getName, (a, b) -> a));
    }

    private static Map<String, String> flowTypeLabels() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("sales_out", "发货");
        map.put("return_in", "退货");
        map.put("manual_out", "手动出库");
        map.put("manual_in", "手动入库");
        map.put("reissue", "补发");
        return map;
    }

    private String resolveCategoryCode(String category) {
        if (!StringUtils.hasText(category)) {
            return null;
        }
        String raw = category.trim();
        try {
            Long id = Long.parseLong(raw);
            return categoryRepository.findById(id)
                    .map(ProductCategory::getCode)
                    .orElse(raw);
        } catch (NumberFormatException e) {
            return raw;
        }
    }

    private static String csv(String v) {
        if (v == null) {
            return "";
        }
        String s = v.replace("\"", "\"\"");
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s + "\"";
        }
        return s;
    }
}
