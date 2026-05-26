package com.dianshang.admin.finance.support;

import com.dianshang.admin.finance.entity.FinTransactionRecord;
import com.dianshang.admin.finance.entity.FinWithdrawApply;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class FinanceSpecifications {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private FinanceSpecifications() {
    }

    public static Specification<FinTransactionRecord> forStatement(
            String keyword, String tradeType, String paymentChannel,
            LocalDateTime start, LocalDateTime end) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("recordNo"), like),
                        cb.like(root.get("orderNo"), like)
                ));
            }
            if (StringUtils.hasText(tradeType)) {
                predicates.add(cb.equal(root.get("tradeType"), tradeType));
            }
            if (StringUtils.hasText(paymentChannel)) {
                predicates.add(cb.equal(root.get("paymentChannel"), paymentChannel));
            }
            if (start != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createTime"), start));
            }
            if (end != null) {
                predicates.add(cb.lessThan(root.get("createTime"), end));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<FinWithdrawApply> forWithdraw(String tab, String keyword) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(tab) && !"all".equals(tab)) {
                switch (tab) {
                    case "pending" -> predicates.add(cb.equal(root.get("verifyStatus"), 0));
                    case "transferring" -> predicates.add(cb.equal(root.get("verifyStatus"), 1));
                    case "done" -> predicates.add(root.get("verifyStatus").in(2, 3, 4));
                    default -> {
                    }
                }
            }
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.trim() + "%";
                predicates.add(cb.like(root.get("applyNo"), like));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static LocalDateTime parseDateTime(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        try {
            return LocalDateTime.parse(text, DT);
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(text + " 00:00:00", DT);
            } catch (Exception ex) {
                return null;
            }
        }
    }
}
