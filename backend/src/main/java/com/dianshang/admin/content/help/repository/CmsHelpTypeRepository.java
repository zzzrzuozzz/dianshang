package com.dianshang.admin.content.help.repository;

import com.dianshang.admin.content.help.entity.CmsHelpTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CmsHelpTypeRepository extends JpaRepository<CmsHelpTypeEntity, Long>,
        JpaSpecificationExecutor<CmsHelpTypeEntity> {

    Optional<CmsHelpTypeEntity> findByIdAndDeletedFalse(Long id);

    long countByDeletedFalse();

    long countByDeletedFalseAndVisibleTrue();

    long countByDeletedFalseAndVisibleFalse();
}
