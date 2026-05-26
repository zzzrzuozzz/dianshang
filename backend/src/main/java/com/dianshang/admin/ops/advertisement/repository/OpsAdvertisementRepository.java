package com.dianshang.admin.ops.advertisement.repository;

import com.dianshang.admin.ops.advertisement.entity.OpsAdvertisementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface OpsAdvertisementRepository extends JpaRepository<OpsAdvertisementEntity, Long>,
        JpaSpecificationExecutor<OpsAdvertisementEntity> {

    Optional<OpsAdvertisementEntity> findByAdvCodeAndDeletedFalse(String advCode);

    long countByDeletedFalse();
}
