package com.dianshang.admin.config;

import com.dianshang.admin.common.Jsons;
import com.dianshang.admin.finance.entity.FinWithdrawApply;
import com.dianshang.admin.finance.repository.FinWithdrawApplyRepository;
import com.dianshang.admin.finance.service.FinanceService;
import com.dianshang.admin.member.entity.MemberEntity;
import com.dianshang.admin.member.repository.MemberRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@Order(100)
public class FinanceDataInitializer implements CommandLineRunner {

    private final FinWithdrawApplyRepository withdrawRepository;
    private final MemberRepository memberRepository;
    private final FinanceService financeService;

    public FinanceDataInitializer(FinWithdrawApplyRepository withdrawRepository,
                                  MemberRepository memberRepository,
                                  FinanceService financeService) {
        this.withdrawRepository = withdrawRepository;
        this.memberRepository = memberRepository;
        this.financeService = financeService;
    }

    @Override
    public void run(String... args) {
        if (withdrawRepository.count() == 0) {
            seedWithdrawals();
        }
        financeService.reconcileGlobal();
    }

    private void seedWithdrawals() {
        List<MemberEntity> members = memberRepository.findByDeletedFalse();
        if (members.isEmpty()) {
            return;
        }
        for (int i = 0; i < Math.min(6, members.size()); i++) {
            MemberEntity m = members.get(i);
            FinWithdrawApply apply = new FinWithdrawApply();
            apply.setApplyNo("WD" + System.currentTimeMillis() + i);
            apply.setUserId(m.getId());
            BigDecimal amount = BigDecimal.valueOf(500 + i * 120L);
            BigDecimal fee = amount.multiply(BigDecimal.valueOf(0.006)).setScale(2, RoundingMode.HALF_UP);
            apply.setApplyAmount(amount);
            apply.setFeeAmount(fee);
            apply.setActualAmount(amount.subtract(fee));
            apply.setBankCardInfo(Jsons.toJson(Map.of(
                    "accountType", i % 2 == 0 ? "BANK" : "ALIPAY",
                    "accountNo", i % 2 == 0 ? "6222 **** **** " + (1000 + i) : "alipay_" + m.getPhone(),
                    "bankName", i % 2 == 0 ? "招商银行" : "支付宝",
                    "holderName", m.getNickname() != null ? m.getNickname() : "商户" + i
            )));
            if (i < 3) {
                apply.setVerifyStatus(0);
            } else if (i == 3) {
                apply.setVerifyStatus(1);
                apply.setVerifyUser("admin");
                apply.setVerifyTime(LocalDateTime.now().minusHours(2));
            } else if (i == 4) {
                apply.setVerifyStatus(3);
                apply.setVerifyUser("admin");
                apply.setVerifyTime(LocalDateTime.now().minusDays(1));
            } else {
                apply.setVerifyStatus(2);
                apply.setVerifyUser("admin");
                apply.setVerifyTime(LocalDateTime.now().minusDays(2));
                apply.setRemark("银行卡信息不匹配");
            }
            apply.setCreateTime(LocalDateTime.now().minusDays(i));
            withdrawRepository.save(apply);
        }
    }
}
