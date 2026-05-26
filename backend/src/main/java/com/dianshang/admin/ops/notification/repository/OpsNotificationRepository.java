package com.dianshang.admin.ops.notification.repository;

import com.dianshang.admin.ops.notification.entity.OpsNotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface OpsNotificationRepository extends JpaRepository<OpsNotificationEntity, String>,
        JpaSpecificationExecutor<OpsNotificationEntity> {

    Optional<OpsNotificationEntity> findByNotifyCodeAndDeletedFalse(String notifyCode);

    long countByDeletedFalse();
}
