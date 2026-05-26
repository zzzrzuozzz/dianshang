package com.dianshang.admin.finance.repository;

import com.dianshang.admin.finance.entity.FinTransactionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FinTransactionRecordRepository extends JpaRepository<FinTransactionRecord, Long>,
        JpaSpecificationExecutor<FinTransactionRecord> {

    Optional<FinTransactionRecord> findByRecordNo(String recordNo);

    boolean existsByOrderNoAndTradeType(String orderNo, String tradeType);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM FinTransactionRecord t WHERE t.status = 1 AND t.amount > 0")
    BigDecimal sumPositiveAmount();

    @Query("SELECT COALESCE(SUM(ABS(t.amount)), 0) FROM FinTransactionRecord t WHERE t.status = 1 AND t.amount < 0")
    BigDecimal sumNegativeAmount();

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM FinTransactionRecord t WHERE t.status = 1 AND t.tradeType = 'REFUND_OUT'")
    BigDecimal sumRefundAmount();

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM FinTransactionRecord t WHERE t.status = 1 AND t.tradeType = 'ORDER_IN' AND t.createTime >= :start AND t.createTime < :end")
    BigDecimal sumOrderInBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(ABS(t.amount)), 0) FROM FinTransactionRecord t WHERE t.status = 1 AND t.tradeType = 'REFUND_OUT' AND t.createTime >= :start AND t.createTime < :end")
    BigDecimal sumRefundBetween(LocalDateTime start, LocalDateTime end);

    List<FinTransactionRecord> findByCreateTimeBetweenOrderByCreateTimeDesc(LocalDateTime start, LocalDateTime end);
}
