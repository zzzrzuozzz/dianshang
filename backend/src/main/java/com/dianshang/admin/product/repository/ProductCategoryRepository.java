package com.dianshang.admin.product.repository;

import com.dianshang.admin.product.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long>, JpaSpecificationExecutor<ProductCategory> {

    List<ProductCategory> findByDeletedFalseOrderBySortNumAsc();

    Optional<ProductCategory> findByCodeAndDeletedFalse(String code);
}
