package com.group.admin.service;

import java.util.List;

/**
 * 權限檢查服務介面
 *
 * @author KUJI System
 * @since 1.0.0
 */
public interface PermissionService {

    /**
     * 檢查用戶是否有某選單的查看權限
     *
     * @param adminUserId 管理者用戶ID (UUID)
     * @param menuCode    選單代碼
     * @return 是否有權限
     */
    boolean canView(String adminUserId, String menuCode);

    /**
     * 檢查用戶是否有某選單的編輯權限
     *
     * @param adminUserId 管理者用戶ID (UUID)
     * @param menuCode    選單代碼
     * @return 是否有權限
     */
    boolean canEdit(String adminUserId, String menuCode);

    /**
     * 檢查用戶是否有某選單的刪除權限
     *
     * @param adminUserId 管理者用戶ID (UUID)
     * @param menuCode    選單代碼
     * @return 是否有權限
     */
    boolean canDelete(String adminUserId, String menuCode);

    /**
     * 檢查用戶是否擁有指定角色
     *
     * @param adminUserId 管理者用戶ID (UUID)
     * @param roleCode    角色代碼
     * @return 是否擁有該角色
     */
    boolean hasRole(String adminUserId, String roleCode);

    /**
     * 檢查用戶是否為 Admin
     *
     * @param adminUserId 管理者用戶ID (UUID)
     * @return 是否為 Admin
     */
    boolean isAdmin(String adminUserId);

    /**
     * 檢查用戶是否為 StoreOwner
     *
     * @param adminUserId 管理者用戶ID (UUID)
     * @return 是否為 StoreOwner
     */
    boolean isStoreOwner(String adminUserId);

    /**
     * 檢查用戶是否為 StoreEditor
     *
     * @param adminUserId 管理者用戶ID (UUID)
     * @return 是否為 StoreEditor
     */
    boolean isStoreEditor(String adminUserId);

    /**
     * 查詢用戶的所有角色ID
     *
     * @param adminUserId 管理者用戶ID (UUID)
     * @return 角色ID列表 (UUID)
     */
    List<String> getUserRoleIds(String adminUserId);

    /**
     * 查詢用戶的所有角色代碼
     *
     * @param adminUserId 管理者用戶ID (UUID)
     * @return 角色代碼列表
     */
    List<String> getUserRoleCodes(String adminUserId);

    /**
     * 查詢用戶可訪問的店鋪ID列表
     * - Admin: 返回 null（表示全部）
     * - StoreOwner/StoreEditor: 返回所屬的店鋪ID
     *
     * @param adminUserId 管理者用戶ID (UUID)
     * @return 店鋪ID列表 (UUID)，null 表示全部
     */
    List<String> getAccessibleStoreIds(String adminUserId);
}
