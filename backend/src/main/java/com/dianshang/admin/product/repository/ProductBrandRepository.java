package com.dianshang.admin.product.repository;

import com.dianshang.admin.product.entity.ProductBrand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ProductBrandRepository extends JpaRepository<ProductBrand, Long>, JpaSpecificationExecutor<ProductBrand> {

    Optional<ProductBrand> findByCodeAndDeletedFalse(String code);
}
