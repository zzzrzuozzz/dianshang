package com.dianshang.admin.product.repository;

import com.dianshang.admin.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Optional<Product> findByProductNoAndDeletedFalse(String productNo);

    long countByDeletedFalse();

    long countByDeletedFalseAndStatus(String status);

    long countByDeletedFalseAndAuditStatus(String auditStatus);

    long countByDeletedFalseAndAuditStatusIn(java.util.Collection<String> statuses);
}
