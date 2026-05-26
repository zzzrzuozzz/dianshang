package com.dianshang.admin.order.support;

import com.dianshang.admin.order.entity.OrderEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public final class OrderSpecifications {

    private OrderSpecifications() {
    }

    public static Specification<OrderEntity> forList(
            String product,
            String orderId,
            String logisticsNo,
            String phone,
            String timeType,
            LocalDate startDate,
            LocalDate endDate,
            String statusTab,
            String pageType) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));

            if ("confirm".equals(pageType)) {
                predicates.add(cb.equal(root.get("orderStatus"), "shipped"));
                predicates.add(cb.notEqual(root.get("shipStatus"), "signed"));
            } else if (StringUtils.hasText(statusTab) && !"all".equals(statusTab)) {
                if ("after_sale".equals(statusTab)) {
                    predicates.add(cb.notEqual(root.get("afterSalesStatus"), "none"));
                } else {
                    predicates.add(cb.equal(root.get("orderStatus"), statusTab));
                }
            }

            if (StringUtils.hasText(product)) {
                String like = "%" + product.trim() + "%";
                predicates.add(cb.like(root.get("productName"), like));
            }
            if (StringUtils.hasText(orderId)) {
                predicates.add(cb.like(root.get("orderNo"), "%" + orderId.trim() + "%"));
            }
            if (StringUtils.hasText(logisticsNo)) {
                String like = "%" + logisticsNo.trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("logisticsNo"), like),
                        cb.like(root.get("logistics"), like)
                ));
            }
            if (StringUtils.hasText(phone)) {
                predicates.add(cb.like(root.get("receiverPhone"), "%" + phone.trim() + "%"));
            }

            if (startDate != null && endDate != null) {
                String field = resolveTimeField(timeType);
                LocalDateTime start = startDate.atStartOfDay();
                LocalDateTime end = endDate.atTime(LocalTime.MAX);
                predicates.add(cb.between(root.get(field), start, end));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static String resolveTimeField(String timeType) {
        if ("pay".equals(timeType)) {
            return "payTime";
        }
        if ("ship".equals(timeType)) {
            return "createTime";
        }
        return "createTime";
    }

    public static Specification<com.dianshang.admin.order.entity.AfterSaleEntity> forAfterSale(
            String product,
            String orderId,
            String logisticsNo,
            String afterSaleId,
            String timeType,
            LocalDate startDate,
            LocalDate endDate,
            String statusTab) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));

            if (StringUtils.hasText(statusTab) && !"all".equals(statusTab)) {
                predicates.add(cb.equal(root.get("afterSaleStatus"), statusTab));
            }
            if (StringUtils.hasText(product)) {
                predicates.add(cb.like(root.get("productName"), "%" + product.trim() + "%"));
            }
            if (StringUtils.hasText(orderId)) {
                predicates.add(cb.like(root.get("orderNo"), "%" + orderId.trim() + "%"));
            }
            if (StringUtils.hasText(afterSaleId)) {
                predicates.add(cb.like(root.get("afterSaleNo"), "%" + afterSaleId.trim() + "%"));
            }
            if (StringUtils.hasText(logisticsNo)) {
                predicates.add(cb.like(root.get("orderNo"), "%" + logisticsNo.trim() + "%"));
            }
            if (startDate != null && endDate != null) {
                String field = "apply".equals(timeType) ? "applyTime" : "applyTime";
                LocalDateTime start = startDate.atStartOfDay();
                LocalDateTime end = endDate.atTime(LocalTime.MAX);
                predicates.add(cb.between(root.get(field), start, end));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
