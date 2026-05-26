package com.dianshang.admin.promotion.repository;

import com.dianshang.admin.promotion.entity.CouponClaimEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface CouponClaimRepository extends JpaRepository<CouponClaimEntity, Long>,
        JpaSpecificationExecutor<CouponClaimEntity> {

    List<CouponClaimEntity> findByCouponCodeOrderByClaimTimeDesc(String couponCode);

    long countByCouponCodeAndStatus(String couponCode, String status);
}
