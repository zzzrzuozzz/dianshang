package com.dianshang.admin.system.service;

import com.dianshang.admin.common.PageResult;
import com.dianshang.admin.system.dto.NoticePageRequest;
import com.dianshang.admin.system.dto.NoticeUnreadSummaryVO;
import com.dianshang.admin.system.dto.NoticeVO;
import com.dianshang.admin.system.entity.SysNotice;
import com.dianshang.admin.system.repository.SysNoticeRepository;
import com.dianshang.admin.system.support.NoticeLabels;
import com.dianshang.admin.system.support.NoticeSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class SysNoticeService {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysNoticeRepository noticeRepository;

    public SysNoticeService(SysNoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    public List<NoticeVO> getTopUnread(int limit, boolean urgent) {
        int size = Math.min(Math.max(limit, 1), 20);
        PageRequest pageable = PageRequest.of(0, size);
        return noticeRepository.findTopUnread(urgent, pageable).stream().map(this::toVO).toList();
    }

    public NoticeUnreadSummaryVO unreadSummary() {
        NoticeUnreadSummaryVO vo = new NoticeUnreadSummaryVO();
        vo.setTotal(noticeRepository.countByStatus(0));
        vo.setSystemCount(noticeRepository.countByStatusAndNoticeType(0, "SYSTEM"));
        vo.setAuditCount(noticeRepository.countByStatusAndNoticeType(0, "AUDIT"));
        vo.setAlarmCount(noticeRepository.countByStatusAndNoticeType(0, "ALARM"));
        vo.setUrgentCount(noticeRepository.countUrgentUnread());
        return vo;
    }

    public PageResult<NoticeVO> queryPage(NoticePageRequest req) {
        Page<SysNotice> page = noticeRepository.findAll(
                NoticeSpecifications.forPage(req.getNoticeType()),
                PageRequest.of(Math.max(req.getPage() - 1, 0), req.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "createTime")));
        List<NoticeVO> list = page.getContent().stream().map(this::toVO).toList();
        return new PageResult<>(list, page.getTotalElements(), req.getPage(), req.getPageSize());
    }

    @Transactional
    public void markAsRead(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        noticeRepository.markReadByIds(ids);
    }

    @Transactional
    public void markAllRead(String noticeType) {
        if (!StringUtils.hasText(noticeType)) {
            noticeRepository.markAllRead();
        } else {
            noticeRepository.markAllReadByType(noticeType);
        }
    }

    @Transactional
    public void deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        noticeRepository.deleteAllById(ids);
    }

    public NoticeVO getDetail(Long id) {
        return noticeRepository.findById(id).map(this::toVO).orElse(null);
    }

    private NoticeVO toVO(SysNotice n) {
        NoticeVO vo = new NoticeVO();
        vo.setId(n.getId());
        vo.setTitle(n.getTitle());
        vo.setContent(n.getContent());
        vo.setNoticeType(n.getNoticeType());
        vo.setNoticeTypeLabel(NoticeLabels.TYPE.getOrDefault(n.getNoticeType(), n.getNoticeType()));
        vo.setLevel(n.getLevel());
        vo.setLevelLabel(NoticeLabels.LEVEL.getOrDefault(n.getLevel(), n.getLevel()));
        vo.setStatus(n.getStatus());
        vo.setSender(n.getSender());
        vo.setCreateTime(n.getCreateTime() != null ? n.getCreateTime().format(DT_FMT) : null);
        return vo;
    }
}
