package com.dianshang.admin.member.repository;

import com.dianshang.admin.member.entity.MemberLevelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberLevelRepository extends JpaRepository<MemberLevelEntity, Long> {

    List<MemberLevelEntity> findByDeletedFalseOrderBySortNumAsc();

    Optional<MemberLevelEntity> findByLevelCodeAndDeletedFalse(String levelCode);

    Optional<MemberLevelEntity> findByIsDefaultTrueAndDeletedFalse();
}
