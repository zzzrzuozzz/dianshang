package com.dianshang.admin.permission.support;

import com.dianshang.admin.permission.entity.SysInviteCode;
import org.springframework.data.jpa.domain.Specification;

public final class InviteCodeSpecifications {

    private InviteCodeSpecifications() {
    }

    public static Specification<SysInviteCode> forPage(Integer used) {
        return (root, query, cb) -> {
            if (used == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("used"), used);
        };
    }
}
