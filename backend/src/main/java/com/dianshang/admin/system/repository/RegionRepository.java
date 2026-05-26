package com.dianshang.admin.system.repository;

import com.dianshang.admin.system.entity.RegionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegionRepository extends JpaRepository<RegionEntity, String> {

    List<RegionEntity> findByParentCodeOrderByCodeAsc(String parentCode);

    Optional<RegionEntity> findByCode(String code);
}
