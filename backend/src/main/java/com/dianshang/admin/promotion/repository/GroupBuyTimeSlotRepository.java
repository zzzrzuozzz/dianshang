package com.dianshang.admin.promotion.repository;

import com.dianshang.admin.promotion.entity.GroupBuyTimeSlotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupBuyTimeSlotRepository extends JpaRepository<GroupBuyTimeSlotEntity, Long> {

    List<GroupBuyTimeSlotEntity> findByActivityCodeOrderByStartTimeAsc(String activityCode);

    Optional<GroupBuyTimeSlotEntity> findByActivityCodeAndSlotCode(String activityCode, String slotCode);

    void deleteByActivityCodeAndSlotCode(String activityCode, String slotCode);
}
