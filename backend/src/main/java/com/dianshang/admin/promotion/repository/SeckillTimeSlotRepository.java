package com.dianshang.admin.promotion.repository;

import com.dianshang.admin.promotion.entity.SeckillTimeSlotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeckillTimeSlotRepository extends JpaRepository<SeckillTimeSlotEntity, Long> {

    List<SeckillTimeSlotEntity> findAllByOrderByStartTimeAsc();

    Optional<SeckillTimeSlotEntity> findBySlotCode(String slotCode);
}
