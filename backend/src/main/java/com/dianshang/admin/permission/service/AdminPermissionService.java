package com.dianshang.admin.permission.service;

import com.dianshang.admin.common.BusinessException;
import com.dianshang.admin.common.PageResult;
import com.dianshang.admin.permission.dto.*;
import com.dianshang.admin.permission.entity.SysAdminRole;
import com.dianshang.admin.permission.entity.SysRole;
import com.dianshang.admin.permission.repository.SysAdminRoleRepository;
import com.dianshang.admin.permission.repository.SysRoleRepository;
import com.dianshang.admin.user.AdminUser;
import com.dianshang.admin.user.AdminUserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminPermissionService {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AdminUserRepository adminUserRepository;
    private final SysRoleRepository roleRepository;
    private final SysAdminRoleRepository adminRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminPermissionService(AdminUserRepository adminUserRepository,
                                  SysRoleRepository roleRepository,
                                  SysAdminRoleRepository adminRoleRepository,
                                  PasswordEncoder passwordEncoder) {
        this.adminUserRepository = adminUserRepository;
        this.roleRepository = roleRepository;
        this.adminRoleRepository = adminRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public PageResult<AdminVO> page(AdminPageRequest req) {
        Specification<AdminUser> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(req.getUsername())) {
                predicates.add(cb.like(cb.lower(root.get("username")),
                        "%" + req.getUsername().trim().toLowerCase() + "%"));
            }
            if (StringUtils.hasText(req.getNickname())) {
                predicates.add(cb.like(cb.lower(root.get("nickname")),
                        "%" + req.getNickname().trim().toLowerCase() + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<AdminUser> page = adminUserRepository.findAll(spec,
                PageRequest.of(Math.max(req.getPage() - 1, 0), req.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "id")));
        Map<Long, List<RoleOptionVO>> roleMap = loadRolesForAdmins(
                page.getContent().stream().map(AdminUser::getId).toList());
        List<AdminVO> list = page.getContent().stream().map(u -> toVO(u, roleMap.get(u.getId()))).toList();
        return new PageResult<>(list, page.getTotalElements(), req.getPage(), req.getPageSize());
    }

    @Transactional
    public void save(AdminSaveRequest req) {
        AdminUser user;
        if (req.getId() != null) {
            user = adminUserRepository.findById(req.getId())
                    .orElseThrow(() -> new BusinessException("管理员不存在"));
            if (!user.getUsername().equals(req.getUsername())
                    && adminUserRepository.findByUsername(req.getUsername()).isPresent()) {
                throw new BusinessException("登录账号已存在");
            }
        } else {
            if (!StringUtils.hasText(req.getPassword())) {
                throw new BusinessException("新建管理员必须设置密码");
            }
            if (adminUserRepository.findByUsername(req.getUsername()).isPresent()) {
                throw new BusinessException("登录账号已存在");
            }
            user = new AdminUser();
        }
        user.setUsername(req.getUsername().trim());
        user.setNickname(req.getNickname());
        user.setStatus(req.getStatus() != null ? req.getStatus() : 1);
        if (StringUtils.hasText(req.getPassword())) {
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        }
        if (user.getAvatar() == null) {
            user.setAvatar("https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png");
        }
        adminUserRepository.save(user);
        bindRoles(user.getId(), req.getRoleIds());
        syncRoleNameDisplay(user);
    }

    @Transactional
    public void updateStatus(Long id, Integer status) {
        AdminUser user = adminUserRepository.findById(id)
                .orElseThrow(() -> new BusinessException("管理员不存在"));
        user.setStatus(status);
        adminUserRepository.save(user);
    }

    private void bindRoles(Long adminId, List<Long> roleIds) {
        adminRoleRepository.deleteByAdminId(adminId);
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        List<SysAdminRole> links = roleIds.stream().distinct().map(rid -> {
            SysAdminRole ar = new SysAdminRole();
            ar.setAdminId(adminId);
            ar.setRoleId(rid);
            return ar;
        }).toList();
        adminRoleRepository.saveAll(links);
    }

    private void syncRoleNameDisplay(AdminUser user) {
        List<SysRole> roles = adminRoleRepository.findByAdminId(user.getId()).stream()
                .map(ar -> roleRepository.findById(ar.getRoleId()).orElse(null))
                .filter(r -> r != null)
                .toList();
        user.setRoleName(roles.isEmpty() ? "未分配" : roles.stream()
                .map(SysRole::getRoleName)
                .collect(Collectors.joining(",")));
        adminUserRepository.save(user);
    }

    private Map<Long, List<RoleOptionVO>> loadRolesForAdmins(List<Long> adminIds) {
        if (adminIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, SysRole> roleCache = roleRepository.findAll().stream()
                .collect(Collectors.toMap(SysRole::getId, r -> r));
        return adminIds.stream().collect(Collectors.toMap(
                id -> id,
                id -> adminRoleRepository.findByAdminId(id).stream()
                        .map(ar -> roleCache.get(ar.getRoleId()))
                        .filter(r -> r != null)
                        .map(r -> {
                            RoleOptionVO o = new RoleOptionVO();
                            o.setId(r.getId());
                            o.setRoleName(r.getRoleName());
                            o.setRoleKey(r.getRoleKey());
                            return o;
                        }).toList()));
    }

    private AdminVO toVO(AdminUser u, List<RoleOptionVO> roles) {
        AdminVO vo = new AdminVO();
        vo.setId(u.getId());
        vo.setUsername(u.getUsername());
        vo.setNickname(u.getNickname());
        vo.setStatus(u.getStatus() != null ? u.getStatus() : 1);
        vo.setCreateTime(u.getCreateTime() != null ? u.getCreateTime().format(DT_FMT) : null);
        vo.setRoles(roles != null ? roles : List.of());
        return vo;
    }
}
