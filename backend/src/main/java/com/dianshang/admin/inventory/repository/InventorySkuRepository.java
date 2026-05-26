package com.dianshang.admin.inventory.repository;

import com.dianshang.admin.inventory.entity.InventorySkuEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventorySkuRepository extends JpaRepository<InventorySkuEntity, Long> {

    List<InventorySkuEntity> findByProductNoAndDeletedFalseOrderByIdAsc(String productNo);

    Optional<InventorySkuEntity> findByProductNoAndSkuLineIdAndDeletedFalse(String productNo, String skuLineId);
}
