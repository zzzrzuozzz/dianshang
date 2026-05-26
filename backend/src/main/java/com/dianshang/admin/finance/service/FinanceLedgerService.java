package com.dianshang.admin.finance.service;

import com.dianshang.admin.finance.entity.FinTransactionRecord;
import com.dianshang.admin.finance.repository.FinTransactionRecordRepository;
import com.dianshang.admin.order.entity.OrderEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class FinanceLedgerService {

    private static final AtomicLong SEQ = new AtomicLong(System.currentTimeMillis() % 100000);

    private final FinTransactionRecordRepository transactionRepository;

    public FinanceLedgerService(FinTransactionRecordRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public FinTransactionRecord recordOrderIncome(OrderEntity order, Long userId) {
        if (order == null || "pending_payment".equals(order.getOrderStatus())) {
            return null;
        }
        if (transactionRepository.existsByOrderNoAndTradeType(order.getOrderNo(), "ORDER_IN")) {
            return null;
        }
        FinTransactionRecord record = new FinTransactionRecord();
        record.setRecordNo(nextRecordNo("FR"));
        record.setOrderNo(order.getOrderNo());
        record.setUserId(userId != null ? userId : 0L);
        record.setTradeType("ORDER_IN");
        record.setAmount(order.getActualAmount());
        record.setPaymentChannel(mapChannel(order.getPayMethod()));
        record.setStatus(1);
        record.setCreateTime(order.getPayTime() != null ? order.getPayTime() : order.getCreateTime());
        return transactionRepository.save(record);
    }

    @Transactional
    public FinTransactionRecord recordRefund(OrderEntity order, Long userId, BigDecimal amount) {
        if (order == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        String key = order.getOrderNo() + "_REFUND";
        if (transactionRepository.existsByOrderNoAndTradeType(key, "REFUND_OUT")) {
            return null;
        }
        FinTransactionRecord record = new FinTransactionRecord();
        record.setRecordNo(nextRecordNo("FR"));
        record.setOrderNo(key);
        record.setUserId(userId != null ? userId : 0L);
        record.setTradeType("REFUND_OUT");
        record.setAmount(amount.negate());
        record.setPaymentChannel(mapChannel(order.getPayMethod()));
        record.setStatus(1);
        record.setCreateTime(LocalDateTime.now());
        return transactionRepository.save(record);
    }

    @Transactional
    public FinTransactionRecord recordWithdraw(Long userId, String applyNo, BigDecimal amount) {
        if (transactionRepository.existsByOrderNoAndTradeType(applyNo, "WITHDRAW")) {
            return null;
        }
        FinTransactionRecord record = new FinTransactionRecord();
        record.setRecordNo(nextRecordNo("FR"));
        record.setOrderNo(applyNo);
        record.setUserId(userId);
        record.setTradeType("WITHDRAW");
        record.setAmount(amount.negate());
        record.setPaymentChannel("BALANCE");
        record.setStatus(1);
        record.setCreateTime(LocalDateTime.now());
        return transactionRepository.save(record);
    }

    public String nextRecordNo(String prefix) {
        String day = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return prefix + day + String.format("%04d", SEQ.incrementAndGet() % 10000);
    }

    private String mapChannel(String payMethod) {
        if (payMethod == null) {
            return "WECHAT";
        }
        if (payMethod.contains("支付宝")) {
            return "ALIPAY";
        }
        if (payMethod.contains("余额")) {
            return "BALANCE";
        }
        return "WECHAT";
    }
}
