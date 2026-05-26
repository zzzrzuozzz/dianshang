package com.dianshang.admin.member.repository;

import com.dianshang.admin.member.entity.MemberLedgerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MemberLedgerRepository extends JpaRepository<MemberLedgerEntity, Long>,
        JpaSpecificationExecutor<MemberLedgerEntity> {
}
