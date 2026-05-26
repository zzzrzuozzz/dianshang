package com.dianshang.admin.promotion.repository;

import com.dianshang.admin.promotion.entity.SeckillActivityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface SeckillActivityRepository extends JpaRepository<SeckillActivityEntity, Long>,
        JpaSpecificationExecutor<SeckillActivityEntity> {

    Optional<SeckillActivityEntity> findByActivityCodeAndDeletedFalse(String activityCode);
}
