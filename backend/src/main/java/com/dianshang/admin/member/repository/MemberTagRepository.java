package com.dianshang.admin.member.repository;

import com.dianshang.admin.member.entity.MemberTagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberTagRepository extends JpaRepository<MemberTagEntity, Long> {

    List<MemberTagEntity> findByDeletedFalseOrderByIdDesc();

    Optional<MemberTagEntity> findByTagCodeAndDeletedFalse(String tagCode);

    boolean existsByTagCode(String tagCode);
}
