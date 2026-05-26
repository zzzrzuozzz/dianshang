package com.dianshang.admin.product.support;

import com.dianshang.admin.product.dto.ProductVO;
import com.dianshang.admin.product.entity.Product;

public final class ProductMapper {

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
}
