package com.dianshang.admin.permission.service;

import com.dianshang.admin.common.BusinessException;
import com.dianshang.admin.permission.dto.MenuSaveRequest;
import com.dianshang.admin.permission.dto.MenuTreeVO;
import com.dianshang.admin.permission.entity.SysMenu;
import com.dianshang.admin.permission.repository.SysMenuRepository;
import com.dianshang.admin.permission.repository.SysRoleMenuRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MenuService {

    private final SysMenuRepository menuRepository;
    private final SysRoleMenuRepository roleMenuRepository;

    public MenuService(SysMenuRepository menuRepository, SysRoleMenuRepository roleMenuRepository) {
        this.menuRepository = menuRepository;
        this.roleMenuRepository = roleMenuRepository;
    }

    public List<MenuTreeVO> tree() {
        return buildTree(menuRepository.findAllByOrderBySortNumAscIdAsc());
    }

    public List<MenuTreeVO> buildTree(List<SysMenu> menus) {
        Map<Long, MenuTreeVO> map = menus.stream().collect(Collectors.toMap(
                SysMenu::getId,
                this::toVO,
                (a, b) -> a,
                LinkedHashMap::new));
        List<MenuTreeVO> roots = new ArrayList<>();
        for (SysMenu m : menus) {
            MenuTreeVO node = map.get(m.getId());
            Long parentId = m.getParentId() != null ? m.getParentId() : 0L;
            if (parentId == 0L) {
                roots.add(node);
            } else if (map.containsKey(parentId)) {
                map.get(parentId).getChildren().add(node);
            } else {
                roots.add(node);
            }
        }
        return roots;
    }

    @Transactional
    public void save(MenuSaveRequest req) {
        validateMenu(req);
        SysMenu menu;
        if (req.getId() != null) {
            menu = menuRepository.findById(req.getId())
                    .orElseThrow(() -> new BusinessException("菜单不存在"));
        } else {
            menu = new SysMenu();
            menu.setCreateTime(LocalDateTime.now());
        }
        menu.setParentId(req.getParentId() != null ? req.getParentId() : 0L);
        menu.setMenuName(req.getMenuName().trim());
        menu.setMenuType(req.getMenuType().trim().toUpperCase());
        menu.setSortNum(req.getSortNum() != null ? req.getSortNum() : 0);
        if ("F".equals(menu.getMenuType())) {
            menu.setPath(null);
            menu.setIcon(null);
            menu.setPerms(StringUtils.hasText(req.getPerms()) ? req.getPerms().trim() : null);
        } else {
            menu.setPath(req.getPath());
            menu.setIcon(req.getIcon());
            menu.setPerms(req.getPerms());
        }
        menuRepository.save(menu);
    }

    @Transactional
    public void delete(Long id) {
        if (menuRepository.countByParentId(id) > 0) {
            throw new BusinessException("存在子菜单，请先删除下级节点");
        }
        if (roleMenuRepository.existsByMenuId(id)) {
            throw new BusinessException("菜单已分配给角色，请先解除角色绑定");
        }
        menuRepository.deleteById(id);
    }

    private void validateMenu(MenuSaveRequest req) {
        String type = req.getMenuType() != null ? req.getMenuType().trim().toUpperCase() : "";
        if (!List.of("M", "C", "F").contains(type)) {
            throw new BusinessException("菜单类型无效");
        }
        if ("F".equals(type) && !StringUtils.hasText(req.getPerms())) {
            throw new BusinessException("按钮类型必须填写权限标识");
        }
        if (!"F".equals(type) && !StringUtils.hasText(req.getPath()) && !"M".equals(type)) {
            throw new BusinessException("菜单类型必须填写路由地址");
        }
    }

    private MenuTreeVO toVO(SysMenu m) {
        MenuTreeVO vo = new MenuTreeVO();
        vo.setId(m.getId());
        vo.setParentId(m.getParentId());
        vo.setMenuName(m.getMenuName());
        vo.setMenuType(m.getMenuType());
        vo.setPath(m.getPath());
        vo.setPerms(m.getPerms());
        vo.setIcon(m.getIcon());
        vo.setSortNum(m.getSortNum());
        return vo;
    }
}
