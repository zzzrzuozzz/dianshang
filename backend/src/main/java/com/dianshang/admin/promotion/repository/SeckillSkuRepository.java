package com.dianshang.admin.promotion.repository;

import com.dianshang.admin.promotion.entity.SeckillSkuEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeckillSkuRepository extends JpaRepository<SeckillSkuEntity, Long> {

    List<SeckillSkuEntity> findByActivityCodeAndSlotCodeOrderBySortNumAsc(String activityCode, String slotCode);

    void deleteByActivityCodeAndSlotCode(String activityCode, String slotCode);

    List<SeckillSkuEntity> findByActivityCode(String activityCode);
}
