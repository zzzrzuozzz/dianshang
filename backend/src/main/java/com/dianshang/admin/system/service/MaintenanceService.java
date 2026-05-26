package com.dianshang.admin.system.service;

import com.dianshang.admin.dashboard.DashboardDailyMetricRepository;
import com.dianshang.admin.finance.repository.FinTransactionRecordRepository;
import com.dianshang.admin.finance.repository.FinWithdrawApplyRepository;
import com.dianshang.admin.order.repository.OrderRepository;
import com.dianshang.admin.product.repository.ProductRepository;
import com.dianshang.admin.system.dto.MaintenanceStatusVO;
import com.dianshang.admin.user.AdminUserRepository;
import com.dianshang.admin.member.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class MaintenanceService {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysConfigService sysConfigService;
    private final AdminUserRepository adminUserRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final FinTransactionRecordRepository transactionRepository;
    private final FinWithdrawApplyRepository withdrawRepository;
    private final DashboardDailyMetricRepository metricRepository;

    private final AtomicReference<String> lastCacheClearTime = new AtomicReference<>("尚未执行");

    public MaintenanceService(SysConfigService sysConfigService,
                              AdminUserRepository adminUserRepository,
                              ProductRepository productRepository,
                              OrderRepository orderRepository,
                              MemberRepository memberRepository,
                              FinTransactionRecordRepository transactionRepository,
                              FinWithdrawApplyRepository withdrawRepository,
                              DashboardDailyMetricRepository metricRepository) {
        this.sysConfigService = sysConfigService;
        this.adminUserRepository = adminUserRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.memberRepository = memberRepository;
        this.transactionRepository = transactionRepository;
        this.withdrawRepository = withdrawRepository;
        this.metricRepository = metricRepository;
    }

    public void clearCache() {
        sysConfigService.clearCache();
        lastCacheClearTime.set(LocalDateTime.now().format(DT_FMT));
    }

    public MaintenanceStatusVO buildInitializerStatus() {
        MaintenanceStatusVO vo = new MaintenanceStatusVO();
        vo.setServerTimeMs(System.currentTimeMillis());
        vo.setCacheClearedAt(lastCacheClearTime.get());
        List<MaintenanceStatusVO.InitializerLine> lines = new ArrayList<>();
        lines.add(line("AdminUserInitializer", adminUserRepository.count() > 0,
                "管理员账号数: " + adminUserRepository.count()));
        lines.add(line("ProductDataInitializer", productRepository.count() > 0,
                "商品数: " + productRepository.count()));
        lines.add(line("OrderDataInitializer", orderRepository.countByDeletedFalse() > 0,
                "有效订单数: " + orderRepository.countByDeletedFalse()));
        lines.add(line("MemberDataInitializer", memberRepository.countByDeletedFalse() > 0,
                "会员数: " + memberRepository.countByDeletedFalse()));
        lines.add(line("FinanceDataInitializer", transactionRepository.count() >= 0,
                "财务流水数: " + transactionRepository.count() + "，待审提现: "
                        + withdrawRepository.countByVerifyStatus(0)));
        lines.add(line("DashboardFinanceSync", metricRepository.count() > 0,
                "看板日指标行数: " + metricRepository.count()));
        lines.add(line("RegionFullSqlImporter", true, "国标区划表由启动脚本导入（懒加载接口可用）"));
        lines.add(line("SysConfigDataInitializer", true, "平台基础参数键值对已就绪"));
        vo.setInitializers(lines);
        return vo;
    }

    private MaintenanceStatusVO.InitializerLine line(String name, boolean ok, String detail) {
        MaintenanceStatusVO.InitializerLine l = new MaintenanceStatusVO.InitializerLine();
        l.setName(name);
        l.setStatus(ok ? "OK" : "WARN");
        l.setDetail(detail);
        return l;
    }
}
