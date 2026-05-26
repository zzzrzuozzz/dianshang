package com.dianshang.admin.order.repository;

import com.dianshang.admin.order.entity.ReturnReasonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ReturnReasonRepository extends JpaRepository<ReturnReasonEntity, Long>, JpaSpecificationExecutor<ReturnReasonEntity> {

    Optional<ReturnReasonEntity> findByReasonCodeAndDeletedFalse(String reasonCode);
}
