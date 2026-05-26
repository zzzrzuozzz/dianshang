package com.dianshang.admin.content.topic.repository;

import com.dianshang.admin.content.topic.entity.CmsTopicCommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CmsTopicCommentRepository extends JpaRepository<CmsTopicCommentEntity, String> {

    List<CmsTopicCommentEntity> findByTopicCodeAndDeletedFalseOrderByCreatedAtDesc(String topicCode);

    Optional<CmsTopicCommentEntity> findByCommentCodeAndDeletedFalse(String commentCode);

    long countByTopicCodeAndDeletedFalse(String topicCode);
}
