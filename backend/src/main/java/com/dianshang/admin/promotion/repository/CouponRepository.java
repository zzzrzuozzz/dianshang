package com.dianshang.admin.promotion.repository;

import com.dianshang.admin.promotion.entity.CouponEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CouponRepository extends JpaRepository<CouponEntity, Long>,
        JpaSpecificationExecutor<CouponEntity> {

    Optional<CouponEntity> findByCouponCodeAndDeletedFalse(String couponCode);
}
