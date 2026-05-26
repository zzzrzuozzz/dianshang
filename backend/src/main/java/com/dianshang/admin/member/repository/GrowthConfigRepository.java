package com.dianshang.admin.member.repository;

import com.dianshang.admin.member.entity.GrowthConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GrowthConfigRepository extends JpaRepository<GrowthConfigEntity, Long> {

    Optional<GrowthConfigEntity> findByConfigKey(String configKey);
}
