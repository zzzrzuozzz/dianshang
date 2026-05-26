package com.dianshang.admin.order.repository;

import com.dianshang.admin.order.entity.ExpressTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ExpressTemplateRepository extends JpaRepository<ExpressTemplateEntity, Long>,
        JpaSpecificationExecutor<ExpressTemplateEntity> {

    Optional<ExpressTemplateEntity> findByTemplateCodeAndDeletedFalse(String templateCode);

    List<ExpressTemplateEntity> findByDeletedFalseAndIsDefaultTrue();
}
