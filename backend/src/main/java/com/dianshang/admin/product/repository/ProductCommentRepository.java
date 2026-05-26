package com.dianshang.admin.product.repository;

import com.dianshang.admin.product.entity.ProductComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ProductCommentRepository extends JpaRepository<ProductComment, Long>, JpaSpecificationExecutor<ProductComment> {

    Optional<ProductComment> findByIdAndDeletedFalse(Long id);

    long countByDeletedFalseAndRating(String rating);

    long countByDeletedFalseAndFeaturedFalse();

    long countByDeletedFalse();
}
