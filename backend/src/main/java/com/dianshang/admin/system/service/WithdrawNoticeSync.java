package com.dianshang.admin.system.service;

import com.dianshang.admin.finance.repository.FinWithdrawApplyRepository;
import com.dianshang.admin.system.entity.SysNotice;
import com.dianshang.admin.system.repository.SysNoticeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class WithdrawNoticeSync {

    private final SysNoticeRepository noticeRepository;
    private final FinWithdrawApplyRepository withdrawRepository;

    public WithdrawNoticeSync(SysNoticeRepository noticeRepository,
                              FinWithdrawApplyRepository withdrawRepository) {
        this.noticeRepository = noticeRepository;
        this.withdrawRepository = withdrawRepository;
    }

    @Transactional
    public void syncWithdrawAuditNotice() {
        long pending = withdrawRepository.findAll().stream()
                .filter(w -> w.getVerifyStatus() != null && w.getVerifyStatus() == 0)
                .count();
        noticeRepository.findAll().stream()
                .filter(n -> "AUDIT".equals(n.getNoticeType()) && n.getTitle().contains("提现"))
                .findFirst()
                .ifPresentOrElse(existing -> updateExisting(existing, pending), () -> createIfNeeded(pending));
    }

    private void updateExisting(SysNotice existing, long pending) {
        if (pending <= 0) {
            existing.setStatus(1);
            noticeRepository.save(existing);
            return;
        }
        existing.setStatus(0);
        existing.setTitle("商户提现待审批（%d 笔）".formatted(pending));
        existing.setContent("""
                <p>当前有 <strong>%d</strong> 笔商户提现申请待审批。</p>
                <p>请前往「财务 → 提现审批」及时处理，避免资金积压。</p>
                """.formatted(pending));
        existing.setLevel(pending >= 3 ? "DANGER" : "WARNING");
        existing.setCreateTime(LocalDateTime.now());
        noticeRepository.save(existing);
    }

    private void createIfNeeded(long pending) {
        if (pending <= 0) {
            return;
        }
        SysNotice n = new SysNotice();
        n.setTitle("商户提现待审批（%d 笔）".formatted(pending));
        n.setContent("""
                <p>当前有 <strong>%d</strong> 笔商户提现申请待审批。</p>
                <p>请前往「财务 → 提现审批」及时处理。</p>
                """.formatted(pending));
        n.setNoticeType("AUDIT");
        n.setLevel(pending >= 3 ? "DANGER" : "WARNING");
        n.setStatus(0);
        n.setSender("财务系统");
        n.setCreateTime(LocalDateTime.now());
        noticeRepository.save(n);
    }
}
