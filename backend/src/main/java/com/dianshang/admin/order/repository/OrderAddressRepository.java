package com.dianshang.admin.order.repository;

import com.dianshang.admin.order.entity.OrderAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface OrderAddressRepository extends JpaRepository<OrderAddressEntity, Long>,
        JpaSpecificationExecutor<OrderAddressEntity> {

    Optional<OrderAddressEntity> findByAddressCodeAndDeletedFalse(String addressCode);

    Optional<OrderAddressEntity> findFirstByAddressTypeAndDeletedFalseAndIsDefaultTrueAndVisibleTrue(
            String addressType);
}
