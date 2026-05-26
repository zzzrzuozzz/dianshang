package com.dianshang.admin.member.repository;

import com.dianshang.admin.member.entity.MemberTagRelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberTagRelRepository extends JpaRepository<MemberTagRelEntity, Long> {

    List<MemberTagRelEntity> findByTagCode(String tagCode);

    List<MemberTagRelEntity> findByUserNo(String userNo);

    long countByTagCode(String tagCode);

    void deleteByTagCodeAndUserNo(String tagCode, String userNo);

    void deleteByTagCode(String tagCode);

    boolean existsByTagCodeAndUserNo(String tagCode, String userNo);
}
