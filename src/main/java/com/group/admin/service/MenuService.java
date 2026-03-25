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
}
