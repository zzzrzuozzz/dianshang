package com.dianshang.admin.order.repository;

import com.dianshang.admin.order.entity.AfterSaleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface AfterSaleRepository extends JpaRepository<AfterSaleEntity, Long>, JpaSpecificationExecutor<AfterSaleEntity> {

    Optional<AfterSaleEntity> findByAfterSaleNoAndDeletedFalse(String afterSaleNo);

    long countByDeletedFalse();

    long countByDeletedFalseAndAfterSaleStatus(String afterSaleStatus);
}
