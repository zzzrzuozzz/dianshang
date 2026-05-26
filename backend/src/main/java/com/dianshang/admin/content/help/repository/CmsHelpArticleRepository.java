package com.dianshang.admin.content.help.repository;

import com.dianshang.admin.content.help.entity.CmsHelpArticleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CmsHelpArticleRepository extends JpaRepository<CmsHelpArticleEntity, String>,
        JpaSpecificationExecutor<CmsHelpArticleEntity> {

    Optional<CmsHelpArticleEntity> findByArticleCodeAndDeletedFalse(String articleCode);

    long countByTypeIdAndDeletedFalse(Long typeId);
}
