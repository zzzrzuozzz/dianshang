package com.dianshang.admin.product.support;

import com.dianshang.admin.product.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> forList(String keyword, String category, String statusTab) {
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
            if (StringUtils.hasText(category)) {
                predicates.add(cb.equal(root.get("categoryCode"), category));
            }
            if (StringUtils.hasText(statusTab) && !"all".equals(statusTab)) {
                switch (statusTab) {
                    case "on", "off" -> predicates.add(cb.equal(root.get("status"), statusTab));
                    case "pending", "rejected" -> predicates.add(cb.equal(root.get("auditStatus"), statusTab));
                    default -> {
                    }
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Product> forAudit(String keyword, String category, String auditTab) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("title"), like),
                        cb.like(root.get("productNo"), like)
                ));
            }
            if (StringUtils.hasText(category)) {
                predicates.add(cb.equal(root.get("categoryCode"), category));
            }
            if (StringUtils.hasText(auditTab) && !"all".equals(auditTab)) {
                switch (auditTab) {
                    case "pending" -> predicates.add(cb.equal(root.get("auditStatus"), "pending"));
                    case "rejected" -> predicates.add(cb.equal(root.get("auditStatus"), "rejected"));
                    case "audited" -> predicates.add(root.get("auditStatus").in("passed", "rejected"));
                    default -> {
                    }
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
