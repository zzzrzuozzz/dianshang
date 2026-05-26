package com.dianshang.admin.finance.repository;

import com.dianshang.admin.finance.entity.FinWithdrawApply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface FinWithdrawApplyRepository extends JpaRepository<FinWithdrawApply, Long>,
        JpaSpecificationExecutor<FinWithdrawApply> {

    Optional<FinWithdrawApply> findByApplyNo(String applyNo);

    List<FinWithdrawApply> findTop5ByVerifyStatusOrderByCreateTimeDesc(Integer verifyStatus);

    long countByVerifyStatus(Integer verifyStatus);
}
