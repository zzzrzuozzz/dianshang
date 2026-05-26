package com.dianshang.admin.order.repository;

import com.dianshang.admin.order.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long>, JpaSpecificationExecutor<OrderEntity> {

    Optional<OrderEntity> findByOrderNoAndDeletedFalse(String orderNo);

    long countByDeletedFalse();

    long countByDeletedFalseAndOrderStatus(String orderStatus);

    long countByDeletedFalseAndOrderStatusAndShipStatusNot(String orderStatus, String shipStatus);

    long countByDeletedFalseAndAfterSalesStatusNot(String afterSalesStatus);
}
