package com.dianshang.admin.member.repository;

import com.dianshang.admin.member.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberEntity, Long>, JpaSpecificationExecutor<MemberEntity> {

    Optional<MemberEntity> findByUserNoAndDeletedFalse(String userNo);

    List<MemberEntity> findByDeletedFalse();

    long countByDeletedFalse();

    boolean existsByUserNo(String userNo);
}
