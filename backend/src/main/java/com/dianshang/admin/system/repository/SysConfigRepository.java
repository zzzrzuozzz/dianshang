package com.dianshang.admin.system.repository;

import com.dianshang.admin.system.entity.SysConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SysConfigRepository extends JpaRepository<SysConfig, Long> {

    Optional<SysConfig> findByConfigKey(String configKey);

    List<SysConfig> findAllByOrderByConfigKeyAsc();
}
