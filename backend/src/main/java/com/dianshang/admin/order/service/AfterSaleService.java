package com.dianshang.admin.order.service;

import com.dianshang.admin.common.TabCountVO;
import com.dianshang.admin.order.dto.AfterSaleListVO;
import com.dianshang.admin.order.dto.AfterSalePageVO;
import com.dianshang.admin.order.entity.AfterSaleEntity;
import com.dianshang.admin.order.repository.AfterSaleRepository;
import com.dianshang.admin.order.support.OrderSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AfterSaleService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-M-d HH:mm");

    private final AfterSaleRepository afterSaleRepository;

    public AfterSaleService(AfterSaleRepository afterSaleRepository) {
        this.afterSaleRepository = afterSaleRepository;
    }

    public AfterSalePageVO list(String product, String orderId, String logisticsNo, String afterSaleId,
                                String timeType, LocalDate start, LocalDate end,
                                String status, int page, int pageSize) {
        Page<AfterSaleEntity> result = afterSaleRepository.findAll(
                OrderSpecifications.forAfterSale(product, orderId, logisticsNo, afterSaleId, timeType, start, end, status),
                PageRequest.of(Math.max(page - 1, 0), pageSize, Sort.by(Sort.Direction.DESC, "applyTime")));

        AfterSalePageVO vo = new AfterSalePageVO();
        vo.setList(result.getContent().stream().map(this::toVO).toList());
        vo.setTotal(result.getTotalElements());
        vo.setPage(page);
        vo.setPageSize(pageSize);
        vo.setTotalPages((int) Math.ceil((double) result.getTotalElements() / pageSize));
        vo.setTabs(buildTabs());
        return vo;
    }

    private List<TabCountVO> buildTabs() {
        return List.of(
                new TabCountVO("platform_pending", "待平台处理", afterSaleRepository.countByDeletedFalseAndAfterSaleStatus("platform_pending")),
                new TabCountVO("user_pending", "待用户处理", afterSaleRepository.countByDeletedFalseAndAfterSaleStatus("user_pending")),
                new TabCountVO("platform_confirm", "待平台确认收货", afterSaleRepository.countByDeletedFalseAndAfterSaleStatus("platform_confirm")),
                new TabCountVO("completed", "已完成", afterSaleRepository.countByDeletedFalseAndAfterSaleStatus("completed")),
                new TabCountVO("rejected", "已拒绝", afterSaleRepository.countByDeletedFalseAndAfterSaleStatus("rejected")),
                new TabCountVO("closed", "已关闭", afterSaleRepository.countByDeletedFalseAndAfterSaleStatus("closed")),
                new TabCountVO("all", "全部", afterSaleRepository.countByDeletedFalse())
        );
    }

    private AfterSaleListVO toVO(AfterSaleEntity a) {
        AfterSaleListVO vo = new AfterSaleListVO();
        vo.setId(a.getAfterSaleNo());
        vo.setOrderId(a.getOrderNo());
        vo.setProductName(a.getProductName());
        vo.setThumb(a.getThumb());
        vo.setOrderStatus(a.getOrderStatus());
        vo.setShipStatus(a.getShipStatus());
        vo.setAfterSaleStatus(a.getAfterSaleStatus());
        vo.setAfterSaleType(a.getAfterSaleType());
        vo.setRefundAmount(a.getRefundAmount());
        vo.setApplyTime(format(a.getApplyTime()));
        vo.setProcessTime(format(a.getProcessTime()));
        return vo;
    }

    private String format(java.time.LocalDateTime time) {
        return time != null ? time.format(FMT) : "";
    }
}
