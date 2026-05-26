package com.dianshang.admin.content.topic.repository;

import com.dianshang.admin.content.topic.entity.CmsTopicEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CmsTopicRepository extends JpaRepository<CmsTopicEntity, String>,
        JpaSpecificationExecutor<CmsTopicEntity> {

    Optional<CmsTopicEntity> findByTopicCodeAndDeletedFalse(String topicCode);

    long countByTypeIdAndDeletedFalse(Long typeId);
}
