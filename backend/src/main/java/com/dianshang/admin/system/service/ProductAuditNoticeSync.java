package com.dianshang.admin.system.service;

import com.dianshang.admin.product.repository.ProductRepository;
import com.dianshang.admin.system.entity.SysNotice;
import com.dianshang.admin.system.repository.SysNoticeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ProductAuditNoticeSync {

    private final SysNoticeRepository noticeRepository;
    private final ProductRepository productRepository;

    public ProductAuditNoticeSync(SysNoticeRepository noticeRepository,
                                    ProductRepository productRepository) {
        this.noticeRepository = noticeRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public void syncPendingAuditNotice() {
        long pending = productRepository.countByDeletedFalseAndAuditStatus("pending");
        noticeRepository.findAll().stream()
                .filter(n -> "AUDIT".equals(n.getNoticeType()) && n.getTitle().contains("待审核"))
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
        existing.setTitle("商品待审核（%d 件）".formatted(pending));
        existing.setContent("""
                <p>当前有 <strong>%d</strong> 件商品等待审核。</p>
                <p>请前往「商品 → 商品审核」及时处理。</p>
                """.formatted(pending));
        existing.setLevel(pending >= 5 ? "DANGER" : "WARNING");
        existing.setCreateTime(LocalDateTime.now());
        noticeRepository.save(existing);
    }

    private void createIfNeeded(long pending) {
        if (pending <= 0) {
            return;
        }
        SysNotice n = new SysNotice();
        n.setTitle("商品待审核（%d 件）".formatted(pending));
        n.setContent("""
                <p>当前有 <strong>%d</strong> 件商品等待审核。</p>
                <p>请前往「商品 → 商品审核」及时处理。</p>
                """.formatted(pending));
        n.setNoticeType("AUDIT");
        n.setLevel(pending >= 5 ? "DANGER" : "WARNING");
        n.setStatus(0);
        n.setSender("商品中心");
        n.setCreateTime(LocalDateTime.now());
        noticeRepository.save(n);
    }
}
