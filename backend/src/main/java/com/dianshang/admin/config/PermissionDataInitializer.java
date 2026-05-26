package com.dianshang.admin.config;

import com.dianshang.admin.permission.entity.SysAdminRole;
import com.dianshang.admin.permission.entity.SysMenu;
import com.dianshang.admin.permission.entity.SysRole;
import com.dianshang.admin.permission.entity.SysRoleMenu;
import com.dianshang.admin.permission.repository.SysAdminRoleRepository;
import com.dianshang.admin.permission.repository.SysMenuRepository;
import com.dianshang.admin.permission.repository.SysRoleMenuRepository;
import com.dianshang.admin.permission.repository.SysRoleRepository;
import com.dianshang.admin.permission.service.PermissionQueryService;
import com.dianshang.admin.user.AdminUser;
import com.dianshang.admin.user.AdminUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Component
@Order(25)
public class PermissionDataInitializer implements CommandLineRunner {

    private final SysMenuRepository menuRepository;
    private final SysRoleRepository roleRepository;
    private final SysRoleMenuRepository roleMenuRepository;
    private final SysAdminRoleRepository adminRoleRepository;
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    public PermissionDataInitializer(SysMenuRepository menuRepository,
                                     SysRoleRepository roleRepository,
                                     SysRoleMenuRepository roleMenuRepository,
                                     SysAdminRoleRepository adminRoleRepository,
                                     AdminUserRepository adminUserRepository,
                                     PasswordEncoder passwordEncoder) {
        this.menuRepository = menuRepository;
        this.roleRepository = roleRepository;
        this.roleMenuRepository = roleMenuRepository;
        this.adminRoleRepository = adminRoleRepository;
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (menuRepository.count() > 0) {
            ensureBuiltinButtonMenus();
            linkDefaultAdminRole();
            roleRepository.findByRoleKey("finance").ifPresent(this::seedFinanceDemoUser);
            return;
        }
        List<SysMenu> allMenus = seedMenus();
        SysRole adminRole = seedRole("超级管理员", PermissionQueryService.SUPER_ADMIN_KEY, 0, "拥有全部菜单");
        SysRole financeRole = seedRole("财务专员", "finance", 10, "仅财务管理相关权限");
        assignAllMenus(adminRole.getId(), allMenus);
        assignFinanceMenus(financeRole.getId(), allMenus);
        linkDefaultAdminRole();
        seedFinanceDemoUser(financeRole);
    }

    private List<SysMenu> seedMenus() {
        List<SysMenu> list = new ArrayList<>();
        SysMenu dashboard = saveMenu(0L, "首页", "C", "/dashboard", null, "HomeFilled", 1);
        list.add(dashboard);

        SysMenu product = saveMenu(0L, "商品", "M", null, null, "Goods", 10);
        list.add(product);
        list.add(saveMenu(product.getId(), "商品列表", "C", "/product/list", null, null, 1));
        list.add(saveMenu(product.getId(), "添加商品", "C", "/product/add", null, null, 2));
        list.add(saveMenu(product.getId(), "商品审核", "C", "/product/audit", null, null, 3));
        list.add(saveMenu(product.getId(), "回收站", "C", "/product/recycle", null, null, 4));
        list.add(saveMenu(product.getId(), "商品评价", "C", "/product/comment", null, null, 5));
        list.add(saveMenu(product.getId(), "商品分类", "C", "/product/category", null, null, 6));
        list.add(saveMenu(product.getId(), "品牌管理", "C", "/product/brand", null, null, 7));

        SysMenu order = saveMenu(0L, "订单", "M", null, null, "List", 20);
        list.add(order);
        SysMenu orderListMenu = saveMenu(order.getId(), "订单列表", "C", "/order/list", null, null, 1);
        list.add(orderListMenu);
        list.add(saveMenu(orderListMenu.getId(), "订单发货", "F", null, "order:ship", null, 1));
        list.add(saveMenu(orderListMenu.getId(), "订单退款", "F", null, "order:refund", null, 2));
        list.add(saveMenu(order.getId(), "确认收货", "C", "/order/confirm", null, null, 2));
        list.add(saveMenu(order.getId(), "售后列表", "C", "/order/after-sale", null, null, 3));
        list.add(saveMenu(order.getId(), "订单设置", "C", "/order/setting", null, null, 4));

        SysMenu inventory = saveMenu(0L, "库存管理", "M", null, null, "Box", 25);
        list.add(inventory);
        list.add(saveMenu(inventory.getId(), "库存看板", "C", "/inventory/list", null, null, 1));
        list.add(saveMenu(inventory.getId(), "出入库流水", "C", "/inventory/flow", null, null, 2));

        SysMenu user = saveMenu(0L, "用户", "M", null, null, "User", 30);
        list.add(user);
        list.add(saveMenu(user.getId(), "用户列表", "C", "/user/list", null, null, 1));
        list.add(saveMenu(user.getId(), "标签管理", "C", "/user/tag", null, null, 2));
        list.add(saveMenu(user.getId(), "会员等级", "C", "/user/level", null, null, 3));
        list.add(saveMenu(user.getId(), "成长值积分", "C", "/user/growth", null, null, 4));

        SysMenu promotion = saveMenu(0L, "营销", "M", null, null, "Ticket", 40);
        list.add(promotion);
        list.add(saveMenu(promotion.getId(), "秒杀活动", "C", "/promotion/seckill", null, null, 1));
        list.add(saveMenu(promotion.getId(), "团购活动", "C", "/promotion/group-buy", null, null, 2));
        list.add(saveMenu(promotion.getId(), "优惠券", "C", "/promotion/coupon", null, null, 3));

        SysMenu ops = saveMenu(0L, "运营", "M", null, null, "Bell", 50);
        list.add(ops);
        list.add(saveMenu(ops.getId(), "系统消息", "C", "/ops/system-message", null, null, 1));
        list.add(saveMenu(ops.getId(), "短信", "C", "/ops/sms", null, null, 2));
        list.add(saveMenu(ops.getId(), "站内信", "C", "/ops/station-message", null, null, 3));
        list.add(saveMenu(ops.getId(), "广告位", "C", "/ops/advertisement", null, null, 4));

        SysMenu content = saveMenu(0L, "内容", "M", null, null, "Document", 60);
        list.add(content);
        list.add(saveMenu(content.getId(), "专题", "C", "/content/topic", null, null, 1));
        list.add(saveMenu(content.getId(), "专题类型", "C", "/content/topic/type", null, null, 2));
        list.add(saveMenu(content.getId(), "帮助", "C", "/content/help", null, null, 3));
        list.add(saveMenu(content.getId(), "帮助分类", "C", "/content/help/type", null, null, 4));

        SysMenu stats = saveMenu(0L, "统计", "M", null, null, "DataAnalysis", 70);
        list.add(stats);
        list.add(saveMenu(stats.getId(), "交易统计", "C", "/stats/transaction", null, null, 1));
        list.add(saveMenu(stats.getId(), "流量统计", "C", "/stats/flow", null, null, 2));
        list.add(saveMenu(stats.getId(), "商品统计", "C", "/stats/product", null, null, 3));

        SysMenu finance = saveMenu(0L, "财务", "M", null, null, "Wallet", 80);
        list.add(finance);
        SysMenu financeIndex = saveMenu(finance.getId(), "资金看板", "C", "/finance/index", null, null, 1);
        SysMenu financeStatement = saveMenu(finance.getId(), "交易流水", "C", "/finance/statement", null, null, 2);
        SysMenu financeWithdraw = saveMenu(finance.getId(), "提现审批", "C", "/finance/withdraw", null, null, 3);
        list.add(financeIndex);
        list.add(financeStatement);
        list.add(financeWithdraw);
        list.add(saveMenu(financeWithdraw.getId(), "提现审核按钮", "F", null, "finance:withdraw:verify", null, 1));

        SysMenu setting = saveMenu(0L, "系统设置", "M", null, null, "Setting", 90);
        list.add(setting);
        list.add(saveMenu(setting.getId(), "平台基础信息", "C", "/setting/index", null, null, 1));
        list.add(saveMenu(setting.getId(), "行政区划", "C", "/setting/region", null, null, 2));
        list.add(saveMenu(setting.getId(), "对账与维护", "C", "/setting/maintenance", null, null, 3));

        SysMenu permission = saveMenu(0L, "权限管理", "M", null, null, "Lock", 100);
        list.add(permission);
        list.add(saveMenu(permission.getId(), "菜单管理", "C", "/permission/menu", null, null, 1));
        list.add(saveMenu(permission.getId(), "角色管理", "C", "/permission/role", null, null, 2));
        list.add(saveMenu(permission.getId(), "管理员", "C", "/permission/user", null, null, 3));

        return menuRepository.findAllByOrderBySortNumAscIdAsc();
    }

    private SysMenu saveMenu(Long parentId, String name, String type, String path, String perms, String icon, int sort) {
        SysMenu m = new SysMenu();
        m.setParentId(parentId);
        m.setMenuName(name);
        m.setMenuType(type);
        m.setPath(path);
        m.setPerms(perms);
        m.setIcon(icon);
        m.setSortNum(sort);
        m.setCreateTime(LocalDateTime.now());
        return menuRepository.save(m);
    }

    private SysRole seedRole(String name, String key, int sort, String remark) {
        return roleRepository.findByRoleKey(key).orElseGet(() -> {
            SysRole r = new SysRole();
            r.setRoleName(name);
            r.setRoleKey(key);
            r.setSortNum(sort);
            r.setStatus(1);
            r.setRemark(remark);
            r.setCreateTime(LocalDateTime.now());
            return roleRepository.save(r);
        });
    }

    private void assignAllMenus(Long roleId, List<SysMenu> menus) {
        roleMenuRepository.deleteByRoleId(roleId);
        menus.forEach(m -> {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(roleId);
            rm.setMenuId(m.getId());
            roleMenuRepository.save(rm);
        });
    }

    private void assignFinanceMenus(Long roleId, List<SysMenu> menus) {
        roleMenuRepository.deleteByRoleId(roleId);
        menus.stream()
                .filter(m -> {
                    if (m.getPath() != null && m.getPath().startsWith("/finance")) {
                        return true;
                    }
                    if ("财务".equals(m.getMenuName())) {
                        return true;
                    }
                    return "finance:withdraw:verify".equals(m.getPerms());
                })
                .forEach(m -> {
                    SysRoleMenu rm = new SysRoleMenu();
                    rm.setRoleId(roleId);
                    rm.setMenuId(m.getId());
                    roleMenuRepository.save(rm);
                });
        menus.stream().filter(m -> "/dashboard".equals(m.getPath())).findFirst().ifPresent(d -> {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(roleId);
            rm.setMenuId(d.getId());
            roleMenuRepository.save(rm);
        });
    }

    /** 旧库增量补全按钮级权限，避免升级后操作按钮缺失 */
    private void ensureBuiltinButtonMenus() {
        List<SysMenu> all = menuRepository.findAllByOrderBySortNumAscIdAsc();
        Predicate<String> hasPerm =
                perm -> all.stream().anyMatch(m -> perm.equals(m.getPerms()));
        all.stream()
                .filter(m -> "/order/list".equals(m.getPath()))
                .findFirst()
                .ifPresent(orderList -> {
                    if (!hasPerm.test("order:ship")) {
                        saveMenu(orderList.getId(), "订单发货", "F", null, "order:ship", null, 1);
                    }
                    if (!hasPerm.test("order:refund")) {
                        saveMenu(orderList.getId(), "订单退款", "F", null, "order:refund", null, 2);
                    }
                });
        all.stream()
                .filter(m -> "/finance/withdraw".equals(m.getPath()))
                .findFirst()
                .ifPresent(withdraw -> {
                    if (!hasPerm.test("finance:withdraw:verify")) {
                        saveMenu(withdraw.getId(), "提现审核按钮", "F", null, "finance:withdraw:verify", null, 1);
                    }
                });
    }

    private void linkDefaultAdminRole() {
        adminUserRepository.findByUsername("admin").ifPresent(admin -> {
            roleRepository.findByRoleKey(PermissionQueryService.SUPER_ADMIN_KEY).ifPresent(role -> {
                if (adminRoleRepository.findByAdminId(admin.getId()).isEmpty()) {
                    SysAdminRole ar = new SysAdminRole();
                    ar.setAdminId(admin.getId());
                    ar.setRoleId(role.getId());
                    adminRoleRepository.save(ar);
                }
                admin.setStatus(1);
                admin.setRoleName("超级管理员");
                adminUserRepository.save(admin);
            });
        });
    }

    private void seedFinanceDemoUser(SysRole financeRole) {
        if (adminUserRepository.findByUsername("finance").isPresent()) {
            return;
        }
        AdminUser u = new AdminUser();
        u.setUsername("finance");
        u.setPassword(passwordEncoder.encode("finance123"));
        u.setNickname("财务专员");
        u.setStatus(1);
        u.setRoleName("财务专员");
        u.setCreateTime(LocalDateTime.now());
        adminUserRepository.save(u);
        SysAdminRole ar = new SysAdminRole();
        ar.setAdminId(u.getId());
        ar.setRoleId(financeRole.getId());
        adminRoleRepository.save(ar);
    }
}
