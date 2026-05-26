package com.dianshang.admin.system.repository;

import com.dianshang.admin.system.entity.SysNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SysNoticeRepository extends JpaRepository<SysNotice, Long>, JpaSpecificationExecutor<SysNotice> {

    long countByStatus(Integer status);

    long countByStatusAndNoticeType(Integer status, String noticeType);

    @Query("""
            select n from SysNotice n
            where n.status = 0
            and (:urgent = false or n.level in ('WARNING', 'DANGER') or n.noticeType in ('AUDIT', 'ALARM'))
            order by n.createTime desc
            """)
    List<SysNotice> findTopUnread(boolean urgent, org.springframework.data.domain.Pageable pageable);

    @Modifying
    @Query("update SysNotice n set n.status = 1 where n.id in :ids")
    int markReadByIds(List<Long> ids);

    @Modifying
    @Query("update SysNotice n set n.status = 1 where n.status = 0")
    int markAllRead();

    @Modifying
    @Query("update SysNotice n set n.status = 1 where n.status = 0 and n.noticeType = :noticeType")
    int markAllReadByType(String noticeType);

    @Query("""
            select count(n) from SysNotice n
            where n.status = 0
            and (n.level in ('WARNING', 'DANGER') or n.noticeType in ('AUDIT', 'ALARM'))
            """)
    long countUrgentUnread();
}
