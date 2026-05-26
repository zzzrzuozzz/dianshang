package com.dianshang.admin.order.repository;

import com.dianshang.admin.order.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long>, JpaSpecificationExecutor<OrderEntity> {

    List<OrderEntity> findByDeletedFalse();

    List<OrderEntity> findByDeletedFalseAndCreateTimeBetween(LocalDateTime start, LocalDateTime end);

    long countByDeletedFalseAndCreateTimeBetween(LocalDateTime start, LocalDateTime end);

    Optional<OrderEntity> findByOrderNoAndDeletedFalse(String orderNo);

    List<OrderEntity> findTop20ByReceiverPhoneAndDeletedFalseOrderByCreateTimeDesc(String receiverPhone);

    long countByDeletedFalse();

    long countByDeletedFalseAndOrderStatus(String orderStatus);

    long countByDeletedFalseAndOrderStatusAndShipStatusNot(String orderStatus, String shipStatus);

    long countByDeletedFalseAndAfterSalesStatusNot(String afterSalesStatus);

    List<OrderEntity> findByDeletedFalseAndOrderStatusAndCreateTimeBefore(String orderStatus, LocalDateTime createTime);
}
