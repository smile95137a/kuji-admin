package com.group.admin.service.impl;

import com.group.admin.entity.Menu;
import com.group.admin.entity.RoleMenu;
import com.group.admin.example.MenuExample;
import com.group.admin.example.RoleMenuExample;
import com.group.admin.exception.BusinessException;
import com.group.admin.mapper.MenuMapper;
import com.group.admin.mapper.RoleMenuMapper;
import com.group.admin.req.menu.MenuCreateReq;
import com.group.admin.req.menu.MenuUpdateReq;
import com.group.admin.res.MenuPermissionRes;
import com.group.admin.res.menu.MenuRes;
import com.group.admin.res.menu.MenuTreeRes;
import com.group.admin.service.MenuService;
import com.group.admin.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 選單服務實作
 * 使用 MyBatis Example 模式進行資料存取
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuMapper menuMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final PermissionService permissionService;

    @Override
    @Transactional
    public MenuRes createMenu(MenuCreateReq req) {
        log.info("建立選單: {}", req.getName());

        // 檢查代碼是否重複 (使用 Example)
        MenuExample codeExample = new MenuExample();
        codeExample.createCriteria().andCodeEqualTo(req.getCode());
        List<Menu> existingMenus = menuMapper.selectByExample(codeExample);
        if (!existingMenus.isEmpty()) {
            throw new BusinessException("選單代碼已存在: " + req.getCode());
        }

        // 如果有父選單，檢查父選單是否存在
        if (req.getParentId() != null) {
            Menu parentMenu = menuMapper.selectByPrimaryKey(req.getParentId());
            if (parentMenu == null) {
                throw new BusinessException("父選單不存在: " + req.getParentId());
            }
        }

        Menu menu = new Menu();
        menu.setId(UUID.randomUUID().toString());
        menu.setName(req.getName());
        menu.setCode(req.getCode());
        menu.setPath(req.getPath());
        menu.setParentId(req.getParentId());
        menu.setIcon(req.getIcon());
        menu.setOrderNum(req.getOrderNum() != null ? req.getOrderNum() : 0);
        menu.setIsVisible(req.getIsVisible() != null ? req.getIsVisible() : true);
        menu.setCreatedAt(LocalDateTime.now());
        menu.setUpdatedAt(LocalDateTime.now());

        menuMapper.insert(menu);
        log.info("選單建立成功: id={}", menu.getId());

        return convertToRes(menu);
    }

    @Override
    @Transactional
    public MenuRes updateMenu(MenuUpdateReq req) {
        log.info("更新選單: id={}", req.getId());

        Menu menu = menuMapper.selectByPrimaryKey(req.getId());
        if (menu == null) {
            throw new BusinessException("選單不存在: " + req.getId());
        }

        // 如果修改了代碼，檢查是否重複 (使用 Example)
        if (req.getCode() != null && !req.getCode().equals(menu.getCode())) {
            MenuExample codeExample = new MenuExample();
            codeExample.createCriteria().andCodeEqualTo(req.getCode());
            List<Menu> existingMenus = menuMapper.selectByExample(codeExample);
            if (!existingMenus.isEmpty()) {
                throw new BusinessException("選單代碼已存在: " + req.getCode());
            }
            menu.setCode(req.getCode());
        }

        // 如果修改了父選單，檢查父選單是否存在且不能形成循環
        if (req.getParentId() != null) {
            if (req.getParentId().equals(req.getId())) {
                throw new BusinessException("父選單不能是自己");
            }
            Menu parentMenu = menuMapper.selectByPrimaryKey(req.getParentId());
            if (parentMenu == null) {
                throw new BusinessException("父選單不存在: " + req.getParentId());
            }
            menu.setParentId(req.getParentId());
        } else {
            menu.setParentId(null);
        }

        if (req.getName() != null) {
            menu.setName(req.getName());
        }
        if (req.getPath() != null) {
            menu.setPath(req.getPath());
        }
        if (req.getIcon() != null) {
            menu.setIcon(req.getIcon());
        }
        if (req.getOrderNum() != null) {
            menu.setOrderNum(req.getOrderNum());
        }
        if (req.getIsVisible() != null) {
            menu.setIsVisible(req.getIsVisible());
        }
        menu.setUpdatedAt(LocalDateTime.now());

        menuMapper.updateByPrimaryKey(menu);
        log.info("選單更新成功: id={}", menu.getId());

        return convertToRes(menu);
    }

    @Override
    @Transactional
    public void deleteMenu(String id) {
        log.info("刪除選單: id={}", id);

        Menu menu = menuMapper.selectByPrimaryKey(id);
        if (menu == null) {
            throw new BusinessException("選單不存在: " + id);
        }

        // 檢查是否有子選單 (使用 Example)
        MenuExample childExample = new MenuExample();
        childExample.createCriteria().andParentIdEqualTo(id);
        List<Menu> children = menuMapper.selectByExample(childExample);
        if (!children.isEmpty()) {
            throw new BusinessException("該選單有子選單，無法刪除");
        }

        // 刪除選單與角色的關聯
        RoleMenuExample roleMenuExample = new RoleMenuExample();
        roleMenuExample.createCriteria().andMenuIdEqualTo(id);
        roleMenuMapper.deleteByExample(roleMenuExample);

        menuMapper.deleteByPrimaryKey(id);
        log.info("選單刪除成功: id={}", id);
    }

    @Override
    public MenuRes getMenuById(String id) {
        Menu menu = menuMapper.selectByPrimaryKey(id);
        if (menu == null) {
            throw new BusinessException("選單不存在: " + id);
        }
        return convertToRes(menu);
    }

    @Override
    public List<MenuRes> getAllMenus() {
        MenuExample example = new MenuExample();
        List<Menu> menus = menuMapper.selectByExample(example);
        return menus.stream()
                .map(this::convertToRes)
                .collect(Collectors.toList());
    }

    @Override
    public List<MenuTreeRes> getMenuTree() {
        MenuExample example = new MenuExample();
        List<Menu> allMenus = menuMapper.selectByExample(example);
        return buildMenuTree(allMenus, null);
    }

    @Override
    public List<MenuTreeRes> getAccessibleMenuTree(String adminUserId) {
        log.info("🔍 [MenuService] 開始查詢選單樹, adminUserId={}", adminUserId);
        
        // 如果是 Admin，返回全部可見選單
        boolean isAdmin = permissionService.isAdmin(adminUserId);
        log.info("🎭 [MenuService] 是否為管理員: {}", isAdmin);
        
        if (isAdmin) {
            MenuExample visibleExample = new MenuExample();
            visibleExample.createCriteria().andIsVisibleEqualTo(true);
            List<Menu> allMenus = menuMapper.selectByExample(visibleExample);
            log.info("📋 [MenuService] 查詢到 {} 個可見選單", allMenus.size());
            List<MenuTreeRes> tree = buildMenuTree(allMenus, null);
            log.info("🌲 [MenuService] 建立選單樹完成, 共 {} 個頂層節點", tree.size());
            return tree;
        }

        // 查詢用戶的角色
        List<String> roleIds = permissionService.getUserRoleIds(adminUserId);
        if (roleIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 查詢角色可訪問的選單ID (使用 Example)
        Set<String> accessibleMenuIds = roleIds.stream()
                .flatMap(roleId -> {
                    RoleMenuExample rmExample = new RoleMenuExample();
                    rmExample.createCriteria()
                            .andRoleIdEqualTo(roleId)
                            .andCanViewEqualTo(true);
                    return roleMenuMapper.selectByExample(rmExample).stream();
                })
                .map(RoleMenu::getMenuId)
                .collect(Collectors.toSet());

        if (accessibleMenuIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 取得所有可見選單，過濾出用戶可訪問的 (使用 Example)
        MenuExample visibleExample = new MenuExample();
        visibleExample.createCriteria().andIsVisibleEqualTo(true);
        List<Menu> allMenus = menuMapper.selectByExample(visibleExample);
        List<Menu> accessibleMenus = allMenus.stream()
                .filter(m -> accessibleMenuIds.contains(m.getId()))
                .collect(Collectors.toList());

        return buildMenuTree(accessibleMenus, null);
    }

    @Override
    public MenuRes getMenuByCode(String code) {
        // 使用 Example 查詢
        MenuExample example = new MenuExample();
        example.createCriteria().andCodeEqualTo(code);
        List<Menu> menus = menuMapper.selectByExample(example);
        if (menus.isEmpty()) {
            throw new BusinessException("選單不存在: " + code);
        }
        return convertToRes(menus.get(0));
    }

    /**
     * 建立選單樹狀結構
     */
    private List<MenuTreeRes> buildMenuTree(List<Menu> menus, String parentId) {
        Map<String, List<Menu>> childrenMap = menus.stream()
                .filter(m -> m.getParentId() != null)
                .collect(Collectors.groupingBy(Menu::getParentId));

        List<Menu> rootMenus = menus.stream()
                .filter(m -> (parentId == null && m.getParentId() == null) ||
                        (parentId != null && parentId.equals(m.getParentId())))
                .sorted((a, b) -> {
                    int orderA = a.getOrderNum() != null ? a.getOrderNum() : 0;
                    int orderB = b.getOrderNum() != null ? b.getOrderNum() : 0;
                    return orderA - orderB;
                })
                .collect(Collectors.toList());

        return rootMenus.stream()
                .map(menu -> {
                    MenuTreeRes node = new MenuTreeRes();
                    node.setId(menu.getId());
                    node.setName(menu.getName());
                    node.setCode(menu.getCode());
                    node.setPath(menu.getPath());
                    node.setIcon(menu.getIcon());
                    node.setOrderNum(menu.getOrderNum());
                    node.setIsVisible(menu.getIsVisible());
                    node.setChildren(buildChildrenTree(menu.getId(), childrenMap));
                    return node;
                })
                .collect(Collectors.toList());
    }

    /**
     * 遞迴建立子選單
     */
    private List<MenuTreeRes> buildChildrenTree(String parentId, Map<String, List<Menu>> childrenMap) {
        List<Menu> children = childrenMap.get(parentId);
        if (children == null || children.isEmpty()) {
            return new ArrayList<>();
        }

        return children.stream()
                .sorted((a, b) -> {
                    int orderA = a.getOrderNum() != null ? a.getOrderNum() : 0;
                    int orderB = b.getOrderNum() != null ? b.getOrderNum() : 0;
                    return orderA - orderB;
                })
                .map(menu -> {
                    MenuTreeRes node = new MenuTreeRes();
                    node.setId(menu.getId());
                    node.setName(menu.getName());
                    node.setCode(menu.getCode());
                    node.setPath(menu.getPath());
                    node.setIcon(menu.getIcon());
                    node.setOrderNum(menu.getOrderNum());
                    node.setIsVisible(menu.getIsVisible());
                    node.setChildren(buildChildrenTree(menu.getId(), childrenMap));
                    return node;
                })
                .collect(Collectors.toList());
    }

    /**
     * 轉換 Entity 為 Response DTO
     */
    private MenuRes convertToRes(Menu menu) {
        MenuRes res = new MenuRes();
        res.setId(menu.getId());
        res.setName(menu.getName());
        res.setCode(menu.getCode());
        res.setPath(menu.getPath());
        res.setParentId(menu.getParentId());
        res.setIcon(menu.getIcon());
        res.setOrderNum(menu.getOrderNum());
        res.setIsVisible(menu.getIsVisible());
        res.setCreatedAt(menu.getCreatedAt());
        res.setUpdatedAt(menu.getUpdatedAt());
        return res;
    }

    // ===== Feature 009: Dynamic menu with permissions =====

    @Override
    public List<MenuPermissionRes> getMenusForCurrentUser(String userId) {
        log.info("🔍 查詢使用者選單權限: userId={}", userId);

        boolean isAdmin = permissionService.isAdmin(userId);
        if (isAdmin) {
            MenuExample visibleExample = new MenuExample();
            visibleExample.createCriteria().andIsVisibleEqualTo(true);
            visibleExample.setOrderByClause("order_num ASC");
            List<Menu> allMenus = menuMapper.selectByExample(visibleExample);

            List<MenuPermissionRes> flatList = allMenus.stream()
                    .map(m -> MenuPermissionRes.builder()
                            .id(m.getId()).name(m.getName()).code(m.getCode())
                            .path(m.getPath()).parentId(m.getParentId())
                            .icon(m.getIcon()).orderNum(m.getOrderNum())
                            .canView(true).canEdit(true).canDelete(true)
                            .build())
                    .collect(Collectors.toList());

            return buildPermissionTree(flatList, null);
        }

        List<Map<String, Object>> rows = menuMapper.getMenusWithPermissionsForUser(userId);
        if (rows == null || rows.isEmpty()) {
            return new ArrayList<>();
        }

        List<MenuPermissionRes> flatList = rows.stream()
                .map(row -> MenuPermissionRes.builder()
                        .id((String) row.get("id"))
                        .name((String) row.get("name"))
                        .code((String) row.get("code"))
                        .path((String) row.get("path"))
                        .parentId((String) row.get("parentId"))
                        .icon((String) row.get("icon"))
                        .orderNum(row.get("orderNum") != null ? ((Number) row.get("orderNum")).intValue() : 0)
                        .canView(toBoolean(row.get("canView")))
                        .canEdit(toBoolean(row.get("canEdit")))
                        .canDelete(toBoolean(row.get("canDelete")))
                        .build())
                .filter(m -> Boolean.TRUE.equals(m.getCanView()))
                .collect(Collectors.toList());

        return buildPermissionTree(flatList, null);
    }

    @Override
    public List<MenuPermissionRes> getAuthorizedMenusForUser(String userId, List<String> roles) {
        log.info("🔍 查詢授權選單: userId={}, roles={}", userId, roles);

        // ROLE_ADMIN 直接取全部可見選單，所有旗標設為 true（跳過 JOIN）
        if (roles != null && roles.contains("ROLE_ADMIN")) {
            MenuExample visibleExample = new MenuExample();
            visibleExample.createCriteria().andIsVisibleEqualTo(true);
            visibleExample.setOrderByClause("order_num ASC");
            List<Menu> allMenus = menuMapper.selectByExample(visibleExample);

            List<MenuPermissionRes> flatList = allMenus.stream()
                    .map(m -> MenuPermissionRes.builder()
                            .id(m.getId()).name(m.getName()).code(m.getCode())
                            .path(m.getPath()).parentId(m.getParentId())
                            .icon(m.getIcon()).orderNum(m.getOrderNum())
                            .canView(true).canEdit(true).canDelete(true)
                            .children(new ArrayList<>())
                            .build())
                    .collect(Collectors.toList());

            return buildPermissionTree(flatList, null);
        }

        // 非 Admin：執行聚合 SQL 取得使用者有效權限
        List<Map<String, Object>> rows = menuMapper.getMenusWithPermissionsForUser(userId);
        if (rows == null || rows.isEmpty()) {
            return new ArrayList<>();
        }

        // 只保留 canView=true 的選單
        List<MenuPermissionRes> flatList = rows.stream()
                .map(row -> MenuPermissionRes.builder()
                        .id((String) row.get("id"))
                        .name((String) row.get("name"))
                        .code((String) row.get("code"))
                        .path((String) row.get("path"))
                        .parentId((String) row.get("parentId"))
                        .icon((String) row.get("icon"))
                        .orderNum(row.get("orderNum") != null ? ((Number) row.get("orderNum")).intValue() : 0)
                        .canView(toBoolean(row.get("canView")))
                        .canEdit(toBoolean(row.get("canEdit")))
                        .canDelete(toBoolean(row.get("canDelete")))
                        .children(new ArrayList<>())
                        .build())
                .filter(m -> Boolean.TRUE.equals(m.getCanView()))
                .collect(Collectors.toList());

        return buildPermissionTree(flatList, null);
    }

    private List<MenuPermissionRes> buildPermissionTree(List<MenuPermissionRes> flatList, String parentId) {
        Map<String, List<MenuPermissionRes>> childrenMap = flatList.stream()
                .filter(m -> m.getParentId() != null)
                .collect(Collectors.groupingBy(MenuPermissionRes::getParentId));

        List<MenuPermissionRes> roots = flatList.stream()
                .filter(m -> (parentId == null && m.getParentId() == null) ||
                        (parentId != null && parentId.equals(m.getParentId())))
                .sorted(Comparator.comparingInt(m -> m.getOrderNum() != null ? m.getOrderNum() : 0))
                .collect(Collectors.toList());

        for (MenuPermissionRes root : roots) {
            root.setChildren(buildPermissionChildren(root.getId(), childrenMap));
        }
        return roots;
    }

    private List<MenuPermissionRes> buildPermissionChildren(String parentId, Map<String, List<MenuPermissionRes>> childrenMap) {
        List<MenuPermissionRes> children = childrenMap.get(parentId);
        if (children == null || children.isEmpty()) {
            return new ArrayList<>();
        }
        children.sort(Comparator.comparingInt(m -> m.getOrderNum() != null ? m.getOrderNum() : 0));
        for (MenuPermissionRes child : children) {
            child.setChildren(buildPermissionChildren(child.getId(), childrenMap));
        }
        return children;
    }

    private Boolean toBoolean(Object val) {
        if (val == null) return false;
        if (val instanceof Boolean) return (Boolean) val;
        if (val instanceof Number) return ((Number) val).intValue() != 0;
        return Boolean.parseBoolean(val.toString());
    }
}
