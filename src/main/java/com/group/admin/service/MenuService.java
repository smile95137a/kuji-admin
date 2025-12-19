package com.group.admin.service;

import com.group.admin.req.menu.MenuCreateReq;
import com.group.admin.req.menu.MenuUpdateReq;
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

    /**
     * 建立選單
     *
     * @param req 建立請求
     * @return 建立後的選單資料
     */
    MenuRes createMenu(MenuCreateReq req);

    /**
     * 更新選單
     *
     * @param req 更新請求
     * @return 更新後的選單資料
     */
    MenuRes updateMenu(MenuUpdateReq req);

    /**
     * 刪除選單
     *
     * @param id 選單ID
     */
    void deleteMenu(String id);

    /**
     * 根據ID查詢選單
     *
     * @param id 選單ID
     * @return 選單資料
     */
    MenuRes getMenuById(String id);

    /**
     * 查詢所有選單（平面列表）
     *
     * @return 選單列表
     */
    List<MenuRes> getAllMenus();

    /**
     * 查詢選單樹狀結構
     *
     * @return 選單樹
     */
    List<MenuTreeRes> getMenuTree();

    /**
     * 根據用戶角色查詢可訪問的選單樹
     *
     * @param adminUserId 管理者用戶ID
     * @return 可訪問的選單樹
     */
    List<MenuTreeRes> getAccessibleMenuTree(String adminUserId);

    /**
     * 根據選單代碼查詢選單
     *
     * @param code 選單代碼
     * @return 選單資料
     */
    MenuRes getMenuByCode(String code);
}
