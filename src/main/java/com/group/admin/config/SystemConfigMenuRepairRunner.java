package com.group.admin.config;

import com.group.admin.entity.Menu;
import com.group.admin.entity.Role;
import com.group.admin.entity.RoleMenu;
import com.group.admin.example.MenuExample;
import com.group.admin.example.RoleExample;
import com.group.admin.example.RoleMenuExample;
import com.group.admin.mapper.MenuMapper;
import com.group.admin.mapper.RoleMapper;
import com.group.admin.mapper.RoleMenuMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(30)
@RequiredArgsConstructor
public class SystemConfigMenuRepairRunner implements CommandLineRunner {

    private static final String ADMIN_ROLE_CODE = "ROLE_ADMIN";

    private static final TopLevelMenuDef SYSTEM_PARENT = new TopLevelMenuDef(
            "SYSTEM_SETTING",
            "系統設定",
            "/home/system-config",
            "setting",
            1,
            Set.of("系統設定", "系統管理"),
            Set.of("/admin/system", "/home/system", "/home/system-config", "/home/system-log"));

    private static final TopLevelMenuDef OPERATION_PARENT = new TopLevelMenuDef(
            "OPERATION_TOOLS",
            "營運工具",
            "/home/recharge-plan",
            "build",
            3,
            Set.of("營運工具"),
            Set.of("/home/system-log", "/home/recharge-plan", "/home/referral-codes"));

    private static final TopLevelMenuDef WEBSITE_PARENT = new TopLevelMenuDef(
            "WEBSITE_CONTENT_MANAGEMENT",
            "網站內容管理",
            "/home/banner",
            "public",
            4,
            Set.of("網站內容管理", "網站內部管理", "站內管理", "內容管理"),
            Set.of("/home/banner", "/home/news", "/home/marquee", "/home/emergency-announcements"));

    private static final List<ChildMenuDef> SYSTEM_CHILDREN = List.of(
            new ChildMenuDef("SYSTEM_CONFIG", "參數設定", "/home/system-config", 1, Set.of()));

    private static final List<ChildMenuDef> OPERATION_CHILDREN = List.of(
            new ChildMenuDef("SYSTEM_LOG", "系統日誌", "/home/system-log", 1, Set.of()),
            new ChildMenuDef("RECHARGE_MANAGE", "儲值方案", "/home/recharge-plan", 2, Set.of("儲值設定")));

    private static final List<ChildMenuDef> WEBSITE_CHILDREN = List.of(
            new ChildMenuDef("BANNER_MANAGE", "Banner 管理", "/home/banner", 1, Set.of()),
            new ChildMenuDef("NEWS_MANAGE", "最新消息", "/home/news", 2, Set.of()),
            new ChildMenuDef("MARQUEE_MANAGE", "跑馬燈管理", "/home/marquee", 3, Set.of()),
            new ChildMenuDef("SYSTEM_NOTICE", "緊急公告", "/home/emergency-announcements", 4, Set.of("系統公告")));

    private final MenuMapper menuMapper;
    private final RoleMapper roleMapper;
    private final RoleMenuMapper roleMenuMapper;

    @Override
    public void run(String... args) {
        LocalDateTime now = LocalDateTime.now();

        Menu systemParent = ensureTopLevelMenu(SYSTEM_PARENT, now);
        Menu operationParent = ensureTopLevelMenu(OPERATION_PARENT, now);
        Menu websiteParent = ensureTopLevelMenu(WEBSITE_PARENT, now);

        Role adminRole = findRoleByCode(ADMIN_ROLE_CODE);
        if (adminRole != null) {
            ensureAdminPermission(adminRole.getId(), systemParent.getId(), now);
            ensureAdminPermission(adminRole.getId(), operationParent.getId(), now);
            ensureAdminPermission(adminRole.getId(), websiteParent.getId(), now);
        }

        upsertChildren(systemParent.getId(), SYSTEM_CHILDREN, adminRole, now);
        upsertChildren(operationParent.getId(), OPERATION_CHILDREN, adminRole, now);
        upsertChildren(websiteParent.getId(), WEBSITE_CHILDREN, adminRole, now);

        log.info("系統選單結構校正完成：系統設定、營運工具、網站內容管理已重新整理");
    }

    private Menu ensureTopLevelMenu(TopLevelMenuDef def, LocalDateTime now) {
        Menu menu = findTopLevelMenu(def);
        if (menu == null) {
            menu = new Menu();
            menu.setId(UUID.randomUUID().toString());
            menu.setCreatedAt(now);
        }

        menu.setName(def.name());
        menu.setCode(def.code());
        menu.setPath(def.path());
        menu.setParentId(null);
        menu.setIcon(def.icon());
        menu.setOrderNum(def.orderNum());
        menu.setIsVisible(true);
        menu.setUpdatedAt(now);
        persistMenu(menu);
        return menu;
    }

    private void upsertChildren(String parentId, List<ChildMenuDef> defs, Role adminRole, LocalDateTime now) {
        for (ChildMenuDef def : defs) {
            Menu child = upsertChild(parentId, def, now);
            if (adminRole != null) {
                ensureAdminPermission(adminRole.getId(), child.getId(), now);
            }
        }
    }

    private Menu upsertChild(String parentId, ChildMenuDef def, LocalDateTime now) {
        Menu menu = findChildMenu(def);
        if (menu == null) {
            menu = new Menu();
            menu.setId(UUID.randomUUID().toString());
            menu.setCreatedAt(now);
        }

        menu.setParentId(parentId);
        menu.setName(def.name());
        menu.setCode(def.code());
        menu.setPath(def.path());
        menu.setOrderNum(def.orderNum());
        menu.setIsVisible(true);
        menu.setUpdatedAt(now);
        persistMenu(menu);
        return menu;
    }

    private Menu findTopLevelMenu(TopLevelMenuDef def) {
        MenuExample example = new MenuExample();
        example.createCriteria().andParentIdIsNull();
        List<Menu> topLevelMenus = menuMapper.selectByExample(example);
        for (Menu menu : topLevelMenus) {
            String code = trim(menu.getCode());
            String name = trim(menu.getName());
            String path = trim(menu.getPath());
            if (def.code().equalsIgnoreCase(code)
                    || def.legacyNames().contains(name)
                    || def.legacyPaths().contains(path)) {
                return menu;
            }
        }
        return null;
    }

    private Menu findChildMenu(ChildMenuDef def) {
        MenuExample example = new MenuExample();
        example.createCriteria();
        List<Menu> menus = menuMapper.selectByExample(example);
        for (Menu menu : menus) {
            String code = trim(menu.getCode());
            String name = trim(menu.getName());
            String path = trim(menu.getPath());
            if (def.code().equalsIgnoreCase(code)
                    || def.path().equals(path)
                    || def.legacyNames().contains(name)) {
                return menu;
            }
        }
        return null;
    }

    private void ensureAdminPermission(String roleId, String menuId, LocalDateTime now) {
        RoleMenuExample example = new RoleMenuExample();
        example.createCriteria().andRoleIdEqualTo(roleId).andMenuIdEqualTo(menuId);
        RoleMenu roleMenu = roleMenuMapper.selectByExample(example).stream().findFirst().orElse(null);

        if (roleMenu == null) {
            roleMenu = new RoleMenu();
            roleMenu.setId(UUID.randomUUID().toString());
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            roleMenu.setCreatedAt(now);
        }

        roleMenu.setCanView(true);
        roleMenu.setCanEdit(true);
        roleMenu.setCanDelete(true);

        if (roleMenuMapper.selectByPrimaryKey(roleMenu.getId()) == null) {
            roleMenuMapper.insert(roleMenu);
        } else {
            roleMenuMapper.updateByPrimaryKeySelective(roleMenu);
        }
    }

    private Role findRoleByCode(String code) {
        RoleExample example = new RoleExample();
        example.createCriteria().andCodeEqualTo(code);
        return roleMapper.selectByExample(example).stream().findFirst().orElse(null);
    }

    private void persistMenu(Menu menu) {
        if (menuMapper.selectByPrimaryKey(menu.getId()) == null) {
            menuMapper.insert(menu);
        } else {
            menuMapper.updateByPrimaryKeySelective(menu);
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private record TopLevelMenuDef(
            String code,
            String name,
            String path,
            String icon,
            int orderNum,
            Set<String> legacyNames,
            Set<String> legacyPaths) {
    }

    private record ChildMenuDef(
            String code,
            String name,
            String path,
            int orderNum,
            Set<String> legacyNames) {
    }
}
