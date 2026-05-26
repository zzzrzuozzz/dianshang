package com.dianshang.admin.member.support;

import com.dianshang.admin.member.entity.MemberEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public final class MemberSpecifications {

    private MemberSpecifications() {
    }

    public static Specification<MemberEntity> listFilter(String account, String nickname,
                                                         String tab, String levelTab,
                                                         LocalDate startDate, LocalDate endDate) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));
            if (account != null && !account.isBlank()) {
                String kw = "%" + account.trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("phone"), kw),
                        cb.like(root.get("userNo"), kw)
                ));
            }
            if (nickname != null && !nickname.isBlank()) {
                predicates.add(cb.like(root.get("nickname"), "%" + nickname.trim() + "%"));
            }
            if (tab != null && !tab.isBlank() && !"all".equals(tab)) {
                predicates.add(cb.equal(root.get("status"), tab));
            }
            if (levelTab != null && !levelTab.isBlank() && !"all".equals(levelTab)) {
                predicates.add(cb.equal(root.get("levelCode"), levelTab));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("registerTime"),
                        LocalDateTime.of(startDate, LocalTime.MIN)));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("registerTime"),
                        LocalDateTime.of(endDate, LocalTime.MAX)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
