package com.dianshang.admin.permission.service;

import com.dianshang.admin.common.BusinessException;
import com.dianshang.admin.common.PageResult;
import com.dianshang.admin.permission.dto.*;
import com.dianshang.admin.permission.entity.SysRole;
import com.dianshang.admin.permission.entity.SysRoleMenu;
import com.dianshang.admin.permission.repository.SysAdminRoleRepository;
import com.dianshang.admin.permission.repository.SysRoleMenuRepository;
import com.dianshang.admin.permission.repository.SysRoleRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class RoleService {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysRoleRepository roleRepository;
    private final SysRoleMenuRepository roleMenuRepository;
    private final SysAdminRoleRepository adminRoleRepository;

    public RoleService(SysRoleRepository roleRepository,
                       SysRoleMenuRepository roleMenuRepository,
                       SysAdminRoleRepository adminRoleRepository) {
        this.roleRepository = roleRepository;
        this.roleMenuRepository = roleMenuRepository;
        this.adminRoleRepository = adminRoleRepository;
    }

    public PageResult<RoleVO> page(RolePageRequest req) {
        Specification<SysRole> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(req.getKeyword())) {
                String kw = "%" + req.getKeyword().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("roleName")), kw),
                        cb.like(cb.lower(root.get("roleKey")), kw)));
            }
            if (req.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), req.getStatus()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<SysRole> page = roleRepository.findAll(spec,
                PageRequest.of(Math.max(req.getPage() - 1, 0), req.getPageSize(),
                        Sort.by(Sort.Direction.ASC, "sortNum", "id")));
        return new PageResult<>(
                page.getContent().stream().map(this::toVO).toList(),
                page.getTotalElements(),
                req.getPage(),
                req.getPageSize());
    }

    public List<RoleOptionVO> options() {
        return roleRepository.findByStatusOrderBySortNumAsc(1).stream().map(r -> {
            RoleOptionVO o = new RoleOptionVO();
            o.setId(r.getId());
            o.setRoleName(r.getRoleName());
            o.setRoleKey(r.getRoleKey());
            return o;
        }).toList();
    }

    public List<Long> menuIdsByRole(Long roleId) {
        return roleMenuRepository.findByRoleId(roleId).stream()
                .map(SysRoleMenu::getMenuId)
                .toList();
    }

    @Transactional
    public void save(RoleSaveRequest req) {
        SysRole role;
        if (req.getId() != null) {
            role = roleRepository.findById(req.getId())
                    .orElseThrow(() -> new BusinessException("角色不存在"));
            if (!role.getRoleKey().equals(req.getRoleKey())
                    && roleRepository.findByRoleKey(req.getRoleKey()).isPresent()) {
                throw new BusinessException("权限字符已存在");
            }
        } else {
            if (roleRepository.findByRoleKey(req.getRoleKey()).isPresent()) {
                throw new BusinessException("权限字符已存在");
            }
            role = new SysRole();
            role.setCreateTime(LocalDateTime.now());
        }
        role.setRoleName(req.getRoleName().trim());
        role.setRoleKey(req.getRoleKey().trim());
        role.setSortNum(req.getSortNum() != null ? req.getSortNum() : 0);
        role.setStatus(req.getStatus() != null ? req.getStatus() : 1);
        role.setRemark(req.getRemark());
        roleRepository.save(role);
    }

    @Transactional
    public void updateStatus(Long id, Integer status) {
        SysRole role = roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("角色不存在"));
        role.setStatus(status);
        roleRepository.save(role);
    }

    /**
     * 安全防重：事务内先清空 role_id 旧关联，再批量插入，避免主键冲突。
     */
    @Transactional
    public void savePermissions(RolePermissionSaveRequest req) {
        if (!roleRepository.existsById(req.getRoleId())) {
            throw new BusinessException("角色不存在");
        }
        roleMenuRepository.deleteByRoleId(req.getRoleId());
        if (req.getMenuIds() == null || req.getMenuIds().isEmpty()) {
            return;
        }
        List<SysRoleMenu> batch = req.getMenuIds().stream().distinct().map(menuId -> {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(req.getRoleId());
            rm.setMenuId(menuId);
            return rm;
        }).toList();
        roleMenuRepository.saveAll(batch);
    }

    @Transactional
    public void delete(Long id) {
        if (adminRoleRepository.countByRoleId(id) > 0) {
            throw new BusinessException("角色已分配给管理员，无法删除");
        }
        roleMenuRepository.deleteByRoleId(id);
        roleRepository.deleteById(id);
    }

    private RoleVO toVO(SysRole r) {
        RoleVO vo = new RoleVO();
        vo.setId(r.getId());
        vo.setRoleName(r.getRoleName());
        vo.setRoleKey(r.getRoleKey());
        vo.setSortNum(r.getSortNum());
        vo.setStatus(r.getStatus());
        vo.setRemark(r.getRemark());
        vo.setCreateTime(r.getCreateTime() != null ? r.getCreateTime().format(DT_FMT) : null);
        return vo;
    }
}
