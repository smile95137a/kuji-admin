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
        List<Menu> menus = menuMapper.selectAll();
        return menus.stream()
                .map(this::convertToRes)
                .collect(Collectors.toList());
    }

    @Override
    public List<MenuTreeRes> getMenuTree() {
        List<Menu> allMenus = menuMapper.selectAll();
        return buildMenuTree(allMenus, null);
    }

    @Override
    public List<MenuTreeRes> getAccessibleMenuTree(String adminUserId) {
        // 如果是 Admin，返回全部可見選單
        if (permissionService.isAdmin(adminUserId)) {
            MenuExample visibleExample = new MenuExample();
            visibleExample.createCriteria().andIsVisibleEqualTo(true);
            List<Menu> allMenus = menuMapper.selectByExample(visibleExample);
            return buildMenuTree(allMenus, null);
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
}
