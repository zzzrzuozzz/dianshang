package com.dianshang.admin.promotion.repository;

import com.dianshang.admin.promotion.entity.GroupBuySkuEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupBuySkuRepository extends JpaRepository<GroupBuySkuEntity, Long> {

    List<GroupBuySkuEntity> findByActivityCodeAndSlotCodeOrderBySortNumAsc(String activityCode, String slotCode);

    void deleteByActivityCodeAndSlotCode(String activityCode, String slotCode);

    List<GroupBuySkuEntity> findByActivityCode(String activityCode);
}
