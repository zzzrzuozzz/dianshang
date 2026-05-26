package com.dianshang.admin.finance.service;

import com.dianshang.admin.member.entity.MemberEntity;
import com.dianshang.admin.member.repository.MemberRepository;
import com.dianshang.admin.order.entity.OrderEntity;
import org.springframework.stereotype.Service;

/**
 * 订单域与财务流水联动：支付/发货/收货后记账，退款时出账。
 */
@Service
public class FinanceOrderBridge {

    private final FinanceLedgerService ledgerService;
    private final MemberRepository memberRepository;

    public FinanceOrderBridge(FinanceLedgerService ledgerService, MemberRepository memberRepository) {
        this.ledgerService = ledgerService;
        this.memberRepository = memberRepository;
    }

    public void recordIncomeIfPaid(OrderEntity order) {
        if (order == null || "pending_payment".equals(order.getOrderStatus())) {
            return;
        }
        ledgerService.recordOrderIncome(order, resolveUserId(order));
    }

    public void recordRefund(OrderEntity order) {
        if (order == null) {
            return;
        }
        ledgerService.recordRefund(order, resolveUserId(order), order.getActualAmount());
    }

    public Long resolveUserId(OrderEntity order) {
        if (order == null || order.getReceiverPhone() == null) {
            return 1L;
        }
        return memberRepository.findFirstByPhoneAndDeletedFalse(order.getReceiverPhone())
                .map(MemberEntity::getId)
                .orElse(1L);
    }
}
