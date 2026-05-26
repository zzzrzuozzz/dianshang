package com.dianshang.admin.finance.service;

import com.dianshang.admin.common.BusinessException;
import com.dianshang.admin.common.Jsons;
import com.fasterxml.jackson.core.type.TypeReference;
import com.dianshang.admin.common.PageResult;
import com.dianshang.admin.finance.dto.*;
import com.dianshang.admin.finance.entity.FinTransactionRecord;
import com.dianshang.admin.finance.entity.FinWithdrawApply;
import com.dianshang.admin.finance.repository.FinTransactionRecordRepository;
import com.dianshang.admin.finance.repository.FinWithdrawApplyRepository;
import com.dianshang.admin.finance.support.FinanceLabels;
import com.dianshang.admin.finance.support.FinanceSpecifications;
import com.dianshang.admin.member.entity.MemberEntity;
import com.dianshang.admin.member.repository.MemberRepository;
import com.dianshang.admin.order.entity.OrderEntity;
import com.dianshang.admin.order.repository.OrderRepository;
import com.dianshang.admin.permission.support.PermissionSecurityHelper;
import com.dianshang.admin.system.service.WithdrawNoticeSync;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FinanceService {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("MM-dd");

    private final FinTransactionRecordRepository transactionRepository;
    private final FinWithdrawApplyRepository withdrawRepository;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final FinanceLedgerService ledgerService;
    private final DashboardFinanceSync dashboardFinanceSync;
    private final PermissionSecurityHelper permissionSecurityHelper;
    private final WithdrawNoticeSync withdrawNoticeSync;

    public FinanceService(FinTransactionRecordRepository transactionRepository,
                         FinWithdrawApplyRepository withdrawRepository,
                         MemberRepository memberRepository,
                         OrderRepository orderRepository,
                         FinanceLedgerService ledgerService,
                         DashboardFinanceSync dashboardFinanceSync,
                         PermissionSecurityHelper permissionSecurityHelper,
                         WithdrawNoticeSync withdrawNoticeSync) {
        this.transactionRepository = transactionRepository;
        this.withdrawRepository = withdrawRepository;
        this.memberRepository = memberRepository;
        this.orderRepository = orderRepository;
        this.ledgerService = ledgerService;
        this.dashboardFinanceSync = dashboardFinanceSync;
        this.permissionSecurityHelper = permissionSecurityHelper;
        this.withdrawNoticeSync = withdrawNoticeSync;
    }

    public Map<String, Object> reconcileGlobal() {
        syncFromOrders();
        syncWithdrawTransactions();
        dashboardFinanceSync.refreshGlobalMetrics();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("transactionCount", transactionRepository.count());
        result.put("pendingWithdrawCount", withdrawRepository.countByVerifyStatus(0));
        return result;
    }

    public void syncWithdrawTransactions() {
        withdrawRepository.findAll().stream()
                .filter(a -> a.getVerifyStatus() != null && a.getVerifyStatus() == 3)
                .forEach(a -> ledgerService.recordWithdraw(a.getUserId(), a.getApplyNo(), a.getApplyAmount()));
    }

    public FinanceOverviewVO overview(String granularity) {
        dashboardFinanceSync.refreshPendingTasks();
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.plusDays(1).atStartOfDay();
        LocalDateTime yesterdayStart = today.minusDays(1).atStartOfDay();

        BigDecimal totalIn = nz(transactionRepository.sumPositiveAmount());
        BigDecimal totalOut = nz(transactionRepository.sumNegativeAmount());
        BigDecimal balance = totalIn.subtract(totalOut);
        BigDecimal pendingSettle = computePendingSettlement();
        BigDecimal todayIn = nz(transactionRepository.sumOrderInBetween(todayStart, todayEnd));
        BigDecimal yesterdayIn = nz(transactionRepository.sumOrderInBetween(yesterdayStart, todayStart));
        BigDecimal totalRefund = nz(transactionRepository.sumRefundAmount());

        FinanceOverviewVO vo = new FinanceOverviewVO();
        vo.setKpis(List.of(
                kpi("balance", "平台总账户余额", balance, trendPercent(todayIn, yesterdayIn), "可提现", "success", "Wallet", "#ecf5ff", "#409eff"),
                kpi("pending", "在途/待结算资金", pendingSettle, 0, "冻结中", "warning", "Clock", "#fdf6ec", "#e6a23c"),
                kpi("today", "今日实收/营业额", todayIn, trendPercent(todayIn, yesterdayIn), "较昨日", "info", "TrendCharts", "#f0f9eb", "#67c23a"),
                kpi("refund", "累计退款金额", totalRefund, 0, "流出", "danger", "RefreshLeft", "#fef0f0", "#f56c6c")
        ));
        vo.setChart(buildChart(granularity));
        vo.setPendingWithdraws(withdrawRepository.findTop5ByVerifyStatusOrderByCreateTimeDesc(0).stream()
                .map(this::toBrief)
                .toList());
        return vo;
    }

    public PageResult<TransactionRecordVO> statementPage(StatementPageRequest req) {
        LocalDateTime start = FinanceSpecifications.parseDateTime(req.getStartTime());
        LocalDateTime end = FinanceSpecifications.parseDateTime(req.getEndTime());
        if (end != null) {
            end = end.plusSeconds(1);
        }
        PageRequest pageable = PageRequest.of(Math.max(req.getPage() - 1, 0), req.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createTime"));
        Page<FinTransactionRecord> page = transactionRepository.findAll(
                FinanceSpecifications.forStatement(req.getKeyword(), req.getTradeType(), req.getPaymentChannel(), start, end),
                pageable);
        return new PageResult<>(
                page.getContent().stream().map(this::toTransactionVO).toList(),
                page.getTotalElements(),
                req.getPage(),
                req.getPageSize()
        );
    }

    public void exportStatement(StatementPageRequest req, HttpServletResponse response) throws Exception {
        req.setPage(1);
        req.setPageSize(5000);
        PageResult<TransactionRecordVO> page = statementPage(req);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=finance_statement.csv");
        try (PrintWriter writer = response.getWriter()) {
            writer.write('\ufeff');
            writer.println("流水号,关联订单号,交易类型,变动金额,支付渠道,记账状态,创建时间");
            for (TransactionRecordVO row : page.getList()) {
                writer.printf("%s,%s,%s,%s,%s,%s,%s%n",
                        esc(row.getRecordNo()), esc(row.getOrderNo()), esc(row.getTradeTypeLabel()),
                        esc(row.getAmountDisplay()), esc(row.getPaymentChannelLabel()),
                        esc(row.getStatusLabel()), esc(row.getCreateTime()));
            }
        }
    }

    public PageResult<WithdrawApplyVO> withdrawPage(WithdrawPageRequest req) {
        PageRequest pageable = PageRequest.of(Math.max(req.getPage() - 1, 0), req.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createTime"));
        Page<FinWithdrawApply> page = withdrawRepository.findAll(
                FinanceSpecifications.forWithdraw(req.getTab(), req.getKeyword()), pageable);
        Map<Long, MemberEntity> members = loadMembers(page.getContent());
        return new PageResult<>(
                page.getContent().stream().map(a -> toWithdrawVO(a, members.get(a.getUserId()))).toList(),
                page.getTotalElements(),
                req.getPage(),
                req.getPageSize()
        );
    }

    @Transactional
    public void verifyWithdraw(WithdrawVerifyRequest req, String operator) {
        permissionSecurityHelper.assertPerm("finance:withdraw:verify");
        FinWithdrawApply apply = withdrawRepository.findByApplyNo(req.getApplyNo())
                .orElseThrow(() -> new BusinessException("提现申请不存在"));
        if (apply.getVerifyStatus() != 0) {
            throw new BusinessException("该申请已处理，不可重复审批（幂等保护）");
        }
        apply.setVerifyUser(operator != null ? operator : "admin");
        apply.setVerifyTime(LocalDateTime.now());
        if (Boolean.TRUE.equals(req.getPassed())) {
            apply.setVerifyStatus(3);
            apply.setRemark(null);
            withdrawRepository.save(apply);
            ledgerService.recordWithdraw(apply.getUserId(), apply.getApplyNo(), apply.getApplyAmount());
            dashboardFinanceSync.refreshGlobalMetrics();
        } else {
            if (!StringUtils.hasText(req.getRemark())) {
                throw new BusinessException("驳回时必须填写原因");
            }
            apply.setVerifyStatus(2);
            apply.setRemark(req.getRemark().trim());
            withdrawRepository.save(apply);
            dashboardFinanceSync.refreshPendingTasks();
        }
        withdrawNoticeSync.syncWithdrawAuditNotice();
    }

    @Transactional
    public void syncFromOrders() {
        List<OrderEntity> orders = orderRepository.findByDeletedFalse();
        Map<String, Long> phoneToUserId = memberRepository.findByDeletedFalse().stream()
                .collect(Collectors.toMap(MemberEntity::getPhone, MemberEntity::getId, (a, b) -> a));
        for (OrderEntity order : orders) {
            Long userId = phoneToUserId.getOrDefault(order.getReceiverPhone(), 1L);
            ledgerService.recordOrderIncome(order, userId);
            if ("refunded".equals(order.getOrderStatus())) {
                ledgerService.recordRefund(order, userId, order.getActualAmount());
            }
        }
    }

    private BigDecimal computePendingSettlement() {
        return orderRepository.findByDeletedFalse().stream()
                .filter(o -> "shipped".equals(o.getOrderStatus()) || "pending_ship".equals(o.getOrderStatus()))
                .map(OrderEntity::getActualAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private FinanceOverviewVO.FinanceChartVO buildChart(String granularity) {
        boolean byMonth = "month".equalsIgnoreCase(granularity);
        int days = byMonth ? 180 : 30;
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(days - 1L);
        Map<LocalDate, BigDecimal[]> bucket = new TreeMap<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            bucket.put(d, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
        }
        LocalDateTime rangeStart = start.atStartOfDay();
        LocalDateTime rangeEnd = end.plusDays(1).atStartOfDay();
        for (FinTransactionRecord t : transactionRepository.findByCreateTimeBetweenOrderByCreateTimeDesc(rangeStart, rangeEnd)) {
            LocalDate day = t.getCreateTime().toLocalDate();
            BigDecimal[] arr = bucket.get(day);
            if (arr == null) continue;
            if ("ORDER_IN".equals(t.getTradeType()) && t.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                arr[0] = arr[0].add(t.getAmount());
            } else if ("REFUND_OUT".equals(t.getTradeType())) {
                arr[1] = arr[1].add(t.getAmount().abs());
            }
        }
        FinanceOverviewVO.FinanceChartVO chart = new FinanceOverviewVO.FinanceChartVO();
        chart.setGranularity(byMonth ? "month" : "day");
        if (byMonth) {
            Map<String, BigDecimal[]> monthMap = new LinkedHashMap<>();
            bucket.forEach((day, vals) -> {
                String key = day.format(DateTimeFormatter.ofPattern("yyyy-MM"));
                monthMap.computeIfAbsent(key, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
                monthMap.get(key)[0] = monthMap.get(key)[0].add(vals[0]);
                monthMap.get(key)[1] = monthMap.get(key)[1].add(vals[1]);
            });
            chart.setDates(new ArrayList<>(monthMap.keySet()));
            chart.setIncomeSeries(monthMap.values().stream().map(a -> (Number) a[0]).toList());
            chart.setRefundSeries(monthMap.values().stream().map(a -> (Number) a[1]).toList());
        } else {
            chart.setDates(bucket.keySet().stream().map(d -> d.format(DAY_FMT)).toList());
            chart.setIncomeSeries(bucket.values().stream().map(a -> (Number) a[0]).toList());
            chart.setRefundSeries(bucket.values().stream().map(a -> (Number) a[1]).toList());
        }
        return chart;
    }

    private FinanceOverviewVO.FinanceKpiVO kpi(String key, String label, BigDecimal value, int trend,
                                               String tag, String tagType, String iconKey, String bg, String color) {
        FinanceOverviewVO.FinanceKpiVO k = new FinanceOverviewVO.FinanceKpiVO();
        k.setKey(key);
        k.setLabel(label);
        k.setValue(value.toPlainString());
        k.setDisplay("¥" + formatMoney(value));
        k.setTrend(trend);
        k.setTag(tag);
        k.setTagType(tagType);
        k.setIconKey(iconKey);
        k.setIconBg(bg);
        k.setIconColor(color);
        return k;
    }

    private FinanceOverviewVO.WithdrawBriefVO toBrief(FinWithdrawApply a) {
        MemberEntity m = memberRepository.findById(a.getUserId()).orElse(null);
        FinanceOverviewVO.WithdrawBriefVO b = new FinanceOverviewVO.WithdrawBriefVO();
        b.setApplyNo(a.getApplyNo());
        b.setMemberName(m != null ? Optional.ofNullable(m.getNickname()).orElse(m.getPhone()) : "用户" + a.getUserId());
        b.setApplyAmount("¥" + formatMoney(a.getApplyAmount()));
        b.setCreateTime(a.getCreateTime().format(DT_FMT));
        return b;
    }

    private TransactionRecordVO toTransactionVO(FinTransactionRecord t) {
        TransactionRecordVO vo = new TransactionRecordVO();
        vo.setRecordNo(t.getRecordNo());
        vo.setOrderNo(t.getOrderNo() != null && t.getOrderNo().contains("_REFUND")
                ? t.getOrderNo().replace("_REFUND", "") : t.getOrderNo());
        vo.setTradeType(t.getTradeType());
        vo.setTradeTypeLabel(FinanceLabels.TRADE_TYPE.getOrDefault(t.getTradeType(), t.getTradeType()));
        vo.setAmount(t.getAmount().toPlainString());
        boolean income = t.getAmount().compareTo(BigDecimal.ZERO) >= 0;
        vo.setAmountDisplay((income ? "+" : "") + formatMoney(t.getAmount()));
        vo.setPaymentChannel(t.getPaymentChannel());
        vo.setPaymentChannelLabel(FinanceLabels.PAYMENT_CHANNEL.getOrDefault(t.getPaymentChannel(), t.getPaymentChannel()));
        vo.setStatus(t.getStatus());
        vo.setStatusLabel(FinanceLabels.TX_STATUS.getOrDefault(t.getStatus(), "未知"));
        vo.setCreateTime(t.getCreateTime().format(DT_FMT));
        return vo;
    }

    @SuppressWarnings("unchecked")
    private WithdrawApplyVO toWithdrawVO(FinWithdrawApply a, MemberEntity m) {
        WithdrawApplyVO vo = new WithdrawApplyVO();
        vo.setApplyNo(a.getApplyNo());
        vo.setUserId(a.getUserId());
        if (m != null) {
            vo.setUserNo(m.getUserNo());
            vo.setNickname(m.getNickname());
            vo.setAvatar(m.getAvatar());
            vo.setShopName(Optional.ofNullable(m.getRemark()).filter(StringUtils::hasText).orElse("自营店铺"));
        }
        vo.setApplyAmount(formatMoney(a.getApplyAmount()));
        vo.setFeeAmount(formatMoney(a.getFeeAmount()));
        vo.setActualAmount(formatMoney(a.getActualAmount()));
        Map<String, Object> bank = parseBank(a.getBankCardInfo());
        vo.setAccountType(String.valueOf(bank.getOrDefault("accountType", "BANK")));
        vo.setAccountNo(String.valueOf(bank.getOrDefault("accountNo", "")));
        vo.setBankName(String.valueOf(bank.getOrDefault("bankName", "")));
        vo.setHolderName(String.valueOf(bank.getOrDefault("holderName", "")));
        vo.setVerifyStatus(a.getVerifyStatus());
        vo.setVerifyStatusLabel(FinanceLabels.WITHDRAW_STATUS.getOrDefault(a.getVerifyStatus(), "未知"));
        vo.setVerifyUser(a.getVerifyUser());
        vo.setVerifyTime(a.getVerifyTime() != null ? a.getVerifyTime().format(DT_FMT) : "");
        vo.setRemark(a.getRemark());
        vo.setCreateTime(a.getCreateTime().format(DT_FMT));
        return vo;
    }

    private Map<String, Object> parseBank(String json) {
        Map<String, Object> map = Jsons.readValue(json, new TypeReference<>() {});
        if (map == null) {
            return Map.of("accountType", "BANK", "accountNo", json != null ? json : "", "bankName", "", "holderName", "");
        }
        return map;
    }

    private Map<Long, MemberEntity> loadMembers(List<FinWithdrawApply> list) {
        Set<Long> ids = list.stream().map(FinWithdrawApply::getUserId).collect(Collectors.toSet());
        Map<Long, MemberEntity> map = new HashMap<>();
        memberRepository.findAllById(ids).forEach(m -> map.put(m.getId(), m));
        return map;
    }

    private BigDecimal nz(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private int trendPercent(BigDecimal current, BigDecimal previous) {
        double c = current.doubleValue();
        double p = previous.doubleValue();
        if (p == 0) return c > 0 ? 100 : 0;
        return (int) Math.round((c - p) / p * 100);
    }

    private String formatMoney(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String esc(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
