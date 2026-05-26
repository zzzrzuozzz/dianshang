package com.dianshang.admin.promotion.repository;

import com.dianshang.admin.promotion.entity.GroupBuyActivityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface GroupBuyActivityRepository extends JpaRepository<GroupBuyActivityEntity, Long>,
        JpaSpecificationExecutor<GroupBuyActivityEntity> {

    Optional<GroupBuyActivityEntity> findByActivityCodeAndDeletedFalse(String activityCode);
}
