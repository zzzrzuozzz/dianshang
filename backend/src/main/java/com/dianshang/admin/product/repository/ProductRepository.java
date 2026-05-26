package com.dianshang.admin.product.repository;

import com.dianshang.admin.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Optional<Product> findByProductNoAndDeletedFalse(String productNo);

    Optional<Product> findByProductNoAndDeletedTrue(String productNo);

    long countByDeletedFalse();

    long countByDeletedTrue();

    long countByDeletedFalseAndStatus(String status);

    long countByDeletedFalseAndAuditStatus(String auditStatus);

    long countByDeletedFalseAndAuditStatusIn(java.util.Collection<String> statuses);

    long countByDeletedFalseAndCategoryCode(String categoryCode);

    java.util.List<Product> findByCategoryCodeAndDeletedFalse(String categoryCode);

    java.util.Optional<Product> findTopByDeletedFalseOrderByIdDesc();
}
