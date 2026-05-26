package com.dianshang.admin.product.support;

import com.dianshang.admin.product.dto.ProductRecycleVO;
import com.dianshang.admin.product.dto.ProductVO;
import com.dianshang.admin.product.entity.Product;

import java.time.format.DateTimeFormatter;

public final class ProductMapper {

    private static final DateTimeFormatter DELETED_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private ProductMapper() {
    }

    public static ProductVO toVO(Product p) {
        ProductVO vo = new ProductVO();
        vo.setId(p.getProductNo());
        vo.setTitle(p.getTitle());
        vo.setSubtitle(p.getSubtitle());
        vo.setThumb(p.getThumb());
        vo.setOriginalPrice(p.getOriginalPrice());
        vo.setDiscountPrice(p.getDiscountPrice());
        vo.setStatus(p.getStatus());
        vo.setAuditStatus(p.getAuditStatus());
        vo.setRemark(p.getRemark() != null ? p.getRemark() : "");
        vo.setSku(p.getSku());
        vo.setSort(p.getSortNum());
        vo.setStock(p.getStock());
        vo.setMonthSales(p.getMonthSales());
        vo.setTotalSales(p.getTotalSales());
        vo.setSupplier(p.getSupplier());
        return vo;
    }

    public static ProductRecycleVO toRecycleVO(Product p) {
        ProductRecycleVO vo = new ProductRecycleVO();
        ProductVO base = toVO(p);
        vo.setId(base.getId());
        vo.setTitle(base.getTitle());
        vo.setSubtitle(base.getSubtitle());
        vo.setThumb(base.getThumb());
        vo.setOriginalPrice(base.getOriginalPrice());
        vo.setDiscountPrice(base.getDiscountPrice());
        vo.setStatus(base.getStatus());
        vo.setAuditStatus(base.getAuditStatus());
        vo.setRemark(base.getRemark());
        vo.setSku(base.getSku());
        vo.setSort(base.getSort());
        vo.setStock(base.getStock());
        vo.setMonthSales(base.getMonthSales());
        vo.setTotalSales(base.getTotalSales());
        vo.setSupplier(base.getSupplier());
        vo.setDeletedAt(p.getDeletedAt() != null ? p.getDeletedAt().format(DELETED_FMT) : "");
        return vo;
    }
}
