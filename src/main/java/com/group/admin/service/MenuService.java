package com.group.admin.service;

import com.group.admin.req.menu.MenuCreateReq;
import com.group.admin.req.menu.MenuUpdateReq;
import com.group.admin.res.MenuPermissionRes;
import com.group.admin.res.menu.MenuRes;
import com.group.admin.res.menu.MenuTreeRes;

import java.util.List;

/**
 * 選單服務介面
 *
 * @author KUJI System
 * @since 1.0.0
 */
public interface MenuService {

    MenuRes createMenu(MenuCreateReq req);

    MenuRes updateMenu(MenuUpdateReq req);

    void deleteMenu(String id);

    MenuRes getMenuById(String id);

    List<MenuRes> getAllMenus();

    List<MenuTreeRes> getMenuTree();

    List<MenuTreeRes> getAccessibleMenuTree(String adminUserId);

    MenuRes getMenuByCode(String code);

    /**
     * 查詢當前使用者可訪問的選單樹（含權限旗標）
     */
    List<MenuPermissionRes> getMenusForCurrentUser(String userId);

    /**
     * 依 userId 與已知角色列表查詢授權選單樹（T025/T026）
     * <p>ROLE_ADMIN 直接取全部可見選單，其他角色透過 JOIN 聚合查詢。</p>
     *
     * @param userId 使用者 ID
     * @param roles  使用者角色列表（完整 ROLE_ 前綴）
     * @return 帶有 canView/canEdit/canDelete 旗標的樹狀選單列表
     */
    List<MenuPermissionRes> getAuthorizedMenusForUser(String userId, List<String> roles);
}
