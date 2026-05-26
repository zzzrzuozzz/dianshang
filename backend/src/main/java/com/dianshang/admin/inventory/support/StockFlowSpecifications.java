package com.dianshang.admin.inventory.support;

import com.dianshang.admin.inventory.entity.StockFlowEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public final class StockFlowSpecifications {

    private StockFlowSpecifications() {
    }

    public static Specification<StockFlowEntity> forFlowList(String flowNo, String productKeyword,
                                                             String bizType, String flowTab,
                                                             String goodsId, LocalDate start,
                                                             LocalDate end) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(flowNo)) {
                String like = "%" + flowNo.trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("flowNo"), like),
                        cb.like(root.get("relatedNo"), like),
                        cb.like(root.get("orderId"), like)
                ));
            }
            if (StringUtils.hasText(goodsId)) {
                predicates.add(cb.equal(root.get("productNo"), goodsId.trim()));
            }
            if (StringUtils.hasText(bizType)) {
                predicates.add(cb.equal(root.get("flowType"), bizType.trim()));
            } else if (StringUtils.hasText(flowTab) && !"all".equals(flowTab)) {
                predicates.add(cb.equal(root.get("flowType"), flowTab));
            }
            if (start != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"),
                        LocalDateTime.of(start, LocalTime.MIN)));
            }
            if (end != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"),
                        LocalDateTime.of(end, LocalTime.MAX)));
            }
            if (StringUtils.hasText(productKeyword)) {
                // 商品名称过滤在 Service 层通过 product_no 关联处理
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
