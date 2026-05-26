package com.dianshang.admin.permission.repository;

import com.dianshang.admin.permission.entity.SysMenu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SysMenuRepository extends JpaRepository<SysMenu, Long> {

    List<SysMenu> findAllByOrderBySortNumAscIdAsc();

    long countByParentId(Long parentId);

    List<SysMenu> findByIdIn(Collection<Long> ids);
}
