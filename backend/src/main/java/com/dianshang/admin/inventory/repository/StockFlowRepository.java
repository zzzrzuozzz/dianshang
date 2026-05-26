package com.dianshang.admin.inventory.repository;

import com.dianshang.admin.inventory.entity.StockFlowEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StockFlowRepository extends JpaRepository<StockFlowEntity, Long>,
        JpaSpecificationExecutor<StockFlowEntity> {

    long countByFlowType(String flowType);

    boolean existsByOrderIdAndFlowType(String orderId, String flowType);
}
