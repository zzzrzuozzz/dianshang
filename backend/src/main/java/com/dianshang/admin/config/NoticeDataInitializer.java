package com.dianshang.admin.config;

import com.dianshang.admin.finance.repository.FinWithdrawApplyRepository;
import com.dianshang.admin.system.entity.SysNotice;
import com.dianshang.admin.system.repository.SysNoticeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@Order(18)
public class NoticeDataInitializer implements CommandLineRunner {

    private final SysNoticeRepository noticeRepository;
    private final FinWithdrawApplyRepository withdrawRepository;

    public NoticeDataInitializer(SysNoticeRepository noticeRepository,
                                   FinWithdrawApplyRepository withdrawRepository) {
        this.noticeRepository = noticeRepository;
        this.withdrawRepository = withdrawRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (noticeRepository.count() == 0) {
            seedDemoNotices();
        }
        syncWithdrawAuditNotice();
    }

    private void seedDemoNotices() {
        save("系统升级公告 v2.6", """
                <p>管理后台将于本周六 02:00-04:00 进行例行升级，期间可能短暂无法访问。</p>
                <p>升级内容：权限模块 RBAC、消息中心、财务对账优化。</p>
                """, "SYSTEM", "INFO", "运维中心");
        save("商品违规待审核提醒", """
                <p>检测到 <strong>3</strong> 件商品涉嫌类目不符或违禁词，请前往商品审核处理。</p>
                """, "AUDIT", "WARNING", "风控引擎");
        save("库存预警：SKU 不足", """
                <p>以下 SKU 库存低于安全线：「暴走T恤-白-L」「限量球鞋-42码」。</p>
                """, "ALARM", "WARNING", "库存监控");
        save("支付通道异常监控", """
                <p>微信支付回调延迟升高，请关注订单支付成功率，必要时联系渠道方。</p>
                """, "ALARM", "DANGER", "监控平台");
        save("新功能上线：消息中心", """
                <p>顶部铃铛可查看未读通知与紧急待办，支持一键已读与批量删除。</p>
                """, "SYSTEM", "INFO", "产品团队");
    }

    private void syncWithdrawAuditNotice() {
        long pending = withdrawRepository.findAll().stream()
                .filter(w -> w.getVerifyStatus() != null && w.getVerifyStatus() == 0)
                .count();
        noticeRepository.findAll().stream()
                .filter(n -> "AUDIT".equals(n.getNoticeType()) && n.getTitle().contains("提现"))
                .findFirst()
                .ifPresentOrElse(existing -> {
                    if (pending <= 0) {
                        existing.setStatus(1);
                        noticeRepository.save(existing);
                        return;
                    }
                    existing.setStatus(0);
                    existing.setContent("""
                            <p>当前有 <strong>%d</strong> 笔商户提现申请待审批。</p>
                            <p>请前往「财务 → 提现审批」及时处理，避免资金积压。</p>
                            """.formatted(pending));
                    existing.setLevel(pending >= 3 ? "DANGER" : "WARNING");
                    existing.setCreateTime(LocalDateTime.now());
                    noticeRepository.save(existing);
                }, () -> {
                    if (pending > 0) {
                        save("商户提现待审批（%d 笔）".formatted(pending), """
                                <p>当前有 <strong>%d</strong> 笔商户提现申请待审批。</p>
                                <p>请前往「财务 → 提现审批」及时处理。</p>
                                """.formatted(pending), "AUDIT", pending >= 3 ? "DANGER" : "WARNING", "财务系统");
                    }
                });
    }

    private void save(String title, String content, String type, String level, String sender) {
        SysNotice n = new SysNotice();
        n.setTitle(title);
        n.setContent(content);
        n.setNoticeType(type);
        n.setLevel(level);
        n.setStatus(0);
        n.setSender(sender);
        n.setCreateTime(LocalDateTime.now());
        noticeRepository.save(n);
    }
}
