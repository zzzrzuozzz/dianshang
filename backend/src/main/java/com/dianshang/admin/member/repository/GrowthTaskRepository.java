package com.dianshang.admin.member.repository;

import com.dianshang.admin.member.entity.GrowthTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GrowthTaskRepository extends JpaRepository<GrowthTaskEntity, Long> {

    List<GrowthTaskEntity> findAllByOrderBySortNumAsc();

    Optional<GrowthTaskEntity> findByTaskCode(String taskCode);
}
