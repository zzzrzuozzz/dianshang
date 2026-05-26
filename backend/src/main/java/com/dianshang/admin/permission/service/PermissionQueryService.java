package com.dianshang.admin.permission.service;

import com.dianshang.admin.permission.dto.MenuTreeVO;
import com.dianshang.admin.permission.entity.SysMenu;
import com.dianshang.admin.permission.entity.SysRole;
import com.dianshang.admin.permission.entity.SysRoleMenu;
import com.dianshang.admin.permission.repository.SysAdminRoleRepository;
import com.dianshang.admin.permission.repository.SysMenuRepository;
import com.dianshang.admin.permission.repository.SysRoleMenuRepository;
import com.dianshang.admin.permission.repository.SysRoleRepository;
import com.dianshang.admin.user.AdminUser;
import com.dianshang.admin.user.AdminUserRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PermissionQueryService {

    public static final String SUPER_ADMIN_KEY = "admin";

    private final AdminUserRepository adminUserRepository;
    private final SysAdminRoleRepository adminRoleRepository;
    private final SysRoleRepository roleRepository;
    private final SysRoleMenuRepository roleMenuRepository;
    private final SysMenuRepository menuRepository;
    private final MenuService menuService;

    public PermissionQueryService(AdminUserRepository adminUserRepository,
                                    SysAdminRoleRepository adminRoleRepository,
                                    SysRoleRepository roleRepository,
                                    SysRoleMenuRepository roleMenuRepository,
                                    SysMenuRepository menuRepository,
                                    MenuService menuService) {
        this.adminUserRepository = adminUserRepository;
        this.adminRoleRepository = adminRoleRepository;
        this.roleRepository = roleRepository;
        this.roleMenuRepository = roleMenuRepository;
        this.menuRepository = menuRepository;
        this.menuService = menuService;
    }

    public List<MenuTreeVO> menusForUser(Long adminId) {
        List<SysMenu> all = menuRepository.findAllByOrderBySortNumAscIdAsc();
        if (isSuperAdmin(adminId)) {
            return filterNavTree(menuService.buildTree(all));
        }
        Set<Long> menuIds = menuIdsForAdmin(adminId);
        if (menuIds.isEmpty()) {
            return List.of();
        }
        Set<Long> expanded = expandAncestors(menuIds, all);
        List<SysMenu> allowed = all.stream().filter(m -> expanded.contains(m.getId())).toList();
        return filterNavTree(menuService.buildTree(allowed));
    }

    public List<String> roleKeysForUser(Long adminId) {
        return rolesForAdmin(adminId).stream().map(SysRole::getRoleKey).distinct().toList();
    }

    public List<String> permsForUser(Long adminId) {
        if (isSuperAdmin(adminId)) {
            return menuRepository.findAllByOrderBySortNumAscIdAsc().stream()
                    .map(SysMenu::getPerms)
                    .filter(Objects::nonNull)
                    .filter(p -> !p.isBlank())
                    .distinct()
                    .toList();
        }
        Set<Long> menuIds = menuIdsForAdmin(adminId);
        return menuRepository.findByIdIn(menuIds).stream()
                .map(SysMenu::getPerms)
                .filter(Objects::nonNull)
                .filter(p -> !p.isBlank())
                .distinct()
                .toList();
    }

    public boolean isSuperAdmin(Long adminId) {
        return rolesForAdmin(adminId).stream()
                .anyMatch(r -> SUPER_ADMIN_KEY.equalsIgnoreCase(r.getRoleKey()));
    }

    public void assertPerm(Long adminId, String perm) {
        if (perm == null || perm.isBlank()) {
            return;
        }
        if (isSuperAdmin(adminId)) {
            return;
        }
        if (!permsForUser(adminId).contains(perm)) {
            throw new com.dianshang.admin.common.BusinessException(403, "无操作权限: " + perm);
        }
    }

    public void assertAdminEnabled(Long adminId) {
        AdminUser user = adminUserRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new com.dianshang.admin.common.BusinessException(403, "账号已禁用，请联系管理员");
        }
    }

    private List<SysRole> rolesForAdmin(Long adminId) {
        return adminRoleRepository.findByAdminId(adminId).stream()
                .map(ar -> roleRepository.findById(ar.getRoleId()).orElse(null))
                .filter(Objects::nonNull)
                .filter(r -> r.getStatus() == null || r.getStatus() == 1)
                .toList();
    }

    private Set<Long> menuIdsForAdmin(Long adminId) {
        List<Long> roleIds = rolesForAdmin(adminId).stream().map(SysRole::getId).toList();
        if (roleIds.isEmpty()) {
            return Set.of();
        }
        return roleMenuRepository.findByRoleIdIn(roleIds).stream()
                .map(SysRoleMenu::getMenuId)
                .collect(Collectors.toSet());
    }

    private Set<Long> expandAncestors(Set<Long> menuIds, List<SysMenu> all) {
        Map<Long, Long> parentMap = all.stream()
                .collect(Collectors.toMap(SysMenu::getId, m -> m.getParentId() != null ? m.getParentId() : 0L));
        Set<Long> expanded = new HashSet<>(menuIds);
        for (Long id : menuIds) {
            Long p = parentMap.get(id);
            while (p != null && p > 0) {
                expanded.add(p);
                p = parentMap.get(p);
            }
        }
        return expanded;
    }

    /** 侧栏仅展示目录与菜单，按钮权限通过 perms 控制 */
    private List<MenuTreeVO> filterNavTree(List<MenuTreeVO> nodes) {
        List<MenuTreeVO> result = new ArrayList<>();
        for (MenuTreeVO n : nodes) {
            if ("F".equals(n.getMenuType())) {
                continue;
            }
            MenuTreeVO copy = copyNode(n);
            copy.setChildren(filterNavTree(n.getChildren()));
            result.add(copy);
        }
        return result;
    }

    private MenuTreeVO copyNode(MenuTreeVO src) {
        MenuTreeVO vo = new MenuTreeVO();
        vo.setId(src.getId());
        vo.setParentId(src.getParentId());
        vo.setMenuName(src.getMenuName());
        vo.setMenuType(src.getMenuType());
        vo.setPath(src.getPath());
        vo.setPerms(src.getPerms());
        vo.setIcon(src.getIcon());
        vo.setSortNum(src.getSortNum());
        return vo;
    }
}
