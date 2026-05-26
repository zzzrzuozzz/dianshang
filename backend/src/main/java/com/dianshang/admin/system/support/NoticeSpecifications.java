package com.dianshang.admin.system.support;

import com.dianshang.admin.system.entity.SysNotice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class NoticeSpecifications {

    private NoticeSpecifications() {
    }

    public static Specification<SysNotice> forPage(String noticeType) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(noticeType)) {
                return cb.conjunction();
            }
            return cb.equal(root.get("noticeType"), noticeType);
        };
    }
}
