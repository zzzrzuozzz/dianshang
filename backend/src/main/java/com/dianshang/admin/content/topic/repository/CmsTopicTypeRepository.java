package com.dianshang.admin.content.topic.repository;

import com.dianshang.admin.content.topic.entity.CmsTopicTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface CmsTopicTypeRepository extends JpaRepository<CmsTopicTypeEntity, Long>,
        JpaSpecificationExecutor<CmsTopicTypeEntity> {

    Optional<CmsTopicTypeEntity> findByIdAndDeletedFalse(Long id);

    long countByDeletedFalse();

    long countByDeletedFalseAndVisibleTrue();

    long countByDeletedFalseAndVisibleFalse();
}
