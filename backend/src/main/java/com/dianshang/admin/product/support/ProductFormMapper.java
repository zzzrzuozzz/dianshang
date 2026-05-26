package com.dianshang.admin.product.support;

import com.dianshang.admin.common.Jsons;
import com.dianshang.admin.product.dto.ProductDetailVO;
import com.dianshang.admin.product.dto.ProductSaveRequest;
import com.dianshang.admin.product.entity.Product;
import com.dianshang.admin.product.entity.ProductBrand;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

public final class ProductFormMapper {

    private static final String DEFAULT_THUMB =
            "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png";

    private ProductFormMapper() {
    }

    public static void applyCreate(Product entity, ProductSaveRequest req, ProductBrand brand, String productNo) {
        entity.setProductNo(productNo);
        applyCommon(entity, req, brand);
        entity.setAuditStatus("pending");
        entity.setMonthSales(0);
        entity.setTotalSales(0);
        entity.setSortNum(0);
        entity.setDeleted(false);
    }

    public static void applyUpdate(Product entity, ProductSaveRequest req, ProductBrand brand) {
        applyCommon(entity, req, brand);
    }

    private static void applyCommon(Product entity, ProductSaveRequest req, ProductBrand brand) {
        entity.setTitle(req.getName().trim());
        entity.setSubtitle(nullToEmpty(req.getSubtitle()));
        entity.setCategoryCode(req.getCategory());
        entity.setBrandCode(req.getBrand());
        entity.setIntro(req.getIntro().trim());
        entity.setShippingTemplate(req.getShipping());
        entity.setDeliveryRegions(Jsons.toJson(emptyToNull(req.getDeliveryRegions())));
        entity.setSku(resolveSku(req.getSku(), entity.getProductNo()));
        entity.setOriginalPrice(defaultDecimal(req.getMarketPrice(), req.getPrice()));
        entity.setDiscountPrice(req.getPrice());
        entity.setStock(req.getStock());
        entity.setStockWarning(req.getStockWarning() != null ? req.getStockWarning() : 0);
        entity.setUnit(req.getUnit());
        entity.setWeight(req.getWeight());
        entity.setPreSale(Boolean.TRUE.equals(req.getPreSale()));
        entity.setRecommendTags(Jsons.joinCsv(req.getRecommend()));
        entity.setServiceTags(Jsons.joinCsv(req.getServices()));
        entity.setProductTags(req.getTags());
        entity.setMainImages(Jsons.toJson(nonEmptyImages(req.getMainImages())));
        entity.setWhiteImage(req.getWhiteImage());
        entity.setVideoUrl(req.getVideo());
        entity.setDetailContent(req.getDetail());
        entity.setThumb(resolveThumb(req.getMainImages()));
        entity.setStatus(Boolean.TRUE.equals(req.getOnSale()) ? "on" : "off");
        entity.setSupplier(brand != null && StringUtils.hasText(brand.getSupplier()) ? brand.getSupplier() : "自营");
    }

    public static ProductDetailVO toDetailVO(Product entity) {
        ProductDetailVO vo = new ProductDetailVO();
        vo.setId(entity.getProductNo());
        vo.setCategory(entity.getCategoryCode());
        vo.setName(entity.getTitle());
        vo.setSubtitle(entity.getSubtitle());
        vo.setBrand(entity.getBrandCode());
        vo.setIntro(entity.getIntro());
        vo.setShipping(entity.getShippingTemplate());
        vo.setDeliveryRegions(Jsons.toStringMatrix(entity.getDeliveryRegions()));
        vo.setSku(entity.getSku());
        vo.setPrice(entity.getDiscountPrice());
        vo.setMarketPrice(entity.getOriginalPrice());
        vo.setStock(entity.getStock());
        vo.setStockWarning(entity.getStockWarning());
        vo.setUnit(entity.getUnit());
        vo.setWeight(entity.getWeight());
        vo.setPreSale(entity.getPreSale());
        vo.setOnSale("on".equals(entity.getStatus()));
        vo.setRecommend(Jsons.splitCsv(entity.getRecommendTags()));
        vo.setServices(Jsons.splitCsv(entity.getServiceTags()));
        vo.setTags(entity.getProductTags());
        vo.setMainImages(Jsons.toStringList(entity.getMainImages()));
        vo.setWhiteImage(entity.getWhiteImage());
        vo.setVideo(entity.getVideoUrl());
        vo.setDetail(entity.getDetailContent());
        vo.setStatus(entity.getStatus());
        vo.setAuditStatus(entity.getAuditStatus());
        return vo;
    }

    private static String resolveSku(String sku, String productNo) {
        if (StringUtils.hasText(sku)) {
            return sku.trim();
        }
        return "SKU-" + productNo;
    }

    private static BigDecimal defaultDecimal(BigDecimal market, BigDecimal price) {
        if (market != null && market.compareTo(BigDecimal.ZERO) > 0) {
            return market;
        }
        return price;
    }

    private static List<String> nonEmptyImages(List<String> images) {
        if (images == null || images.isEmpty()) {
            return List.of(DEFAULT_THUMB);
        }
        return images.stream().filter(StringUtils::hasText).toList();
    }

    private static String resolveThumb(List<String> images) {
        List<String> list = nonEmptyImages(images);
        return list.get(0);
    }

    private static String nullToEmpty(String value) {
        return value != null ? value : "";
    }

    private static Object emptyToNull(List<?> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list;
    }
}
