package com.dianshang.admin.inventory.support;

import com.dianshang.admin.product.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class InventorySpecifications {

    private InventorySpecifications() {
    }

    public static Specification<Product> forInventoryList(String keyword, String categoryCode,
                                                          String supplierKey, String stockStatus,
                                                          String tab) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));

            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("title"), like),
                        cb.like(root.get("productNo"), like),
                        cb.like(root.get("sku"), like)
                ));
            }
            if (StringUtils.hasText(categoryCode)) {
                predicates.add(cb.equal(root.get("categoryCode"), categoryCode));
            }
            if (StringUtils.hasText(supplierKey)) {
                predicates.add(cb.equal(root.get("supplier"), mapSupplier(supplierKey)));
            }
            if (StringUtils.hasText(stockStatus)) {
                predicates.add(stockStatusPredicate(root, cb, stockStatus));
            }
            if ("warning".equals(tab)) {
                predicates.add(cb.greaterThan(root.get("stock"), 0));
                predicates.add(cb.lessThanOrEqualTo(root.get("stock"), root.get("stockWarning")));
            } else if ("out".equals(tab)) {
                predicates.add(cb.equal(root.get("stock"), 0));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static Predicate stockStatusPredicate(jakarta.persistence.criteria.Root<Product> root,
                                                  jakarta.persistence.criteria.CriteriaBuilder cb,
                                                  String stockStatus) {
        return switch (stockStatus) {
            case "warning" -> cb.and(
                    cb.greaterThan(root.get("stock"), 0),
                    cb.lessThanOrEqualTo(root.get("stock"), root.get("stockWarning")));
            case "out" -> cb.equal(root.get("stock"), 0);
            case "sufficient" -> cb.and(
                    cb.greaterThan(root.get("stock"), root.get("stockWarning")));
            default -> cb.conjunction();
        };
    }

    private static String mapSupplier(String key) {
        return switch (key) {
            case "self" -> "自营";
            case "third" -> "第三方";
            case "bangshou" -> "邦手";
            default -> key;
        };
    }
}
