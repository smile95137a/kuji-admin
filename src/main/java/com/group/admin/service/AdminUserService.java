package com.group.admin.service;

import com.group.admin.req.admin.CreateStoreEditorReq;
import com.group.admin.req.admin.CreateStoreOwnerReq;
import com.group.admin.res.admin.AdminUserRes;

import java.util.List;

/**
 * 後台帳號管理服務介面
 * 
 * <p>提供 Admin 管理 StoreOwner、StoreEditor 帳號的功能</p>
 * <p>依據 store-account-management.prompt.md：</p>
 * <ul>
 *   <li>只有 Admin 可以建立/停用 StoreOwner 及 StoreEditor 帳號</li>
 *   <li>StoreOwner/StoreEditor 無法自行修改權限</li>
 *   <li>帳號建立後需發送初始密碼通知</li>
 * </ul>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
public interface AdminUserService {

    /**
     * 建立店家負責人帳號
     * 
     * <p>由 Admin 建立 StoreOwner 帳號，同時建立對應的 Store</p>
     * 
     * @param req 建立店家負責人請求
     * @return 建立的帳號資訊
     */
    AdminUserRes createStoreOwner(CreateStoreOwnerReq req);

    /**
     * 建立店家編輯人員帳號
     * 
     * <p>由 Admin 建立 StoreEditor 帳號，綁定到指定的 Store</p>
     * 
     * @param req 建立店家編輯人員請求
     * @return 建立的帳號資訊
     */
    AdminUserRes createStoreEditor(CreateStoreEditorReq req);

    /**
     * 取得帳號詳情
     * 
     * @param userId 帳號 ID
     * @return 帳號資訊
     */
    AdminUserRes getAdminUser(String userId);

    /**
     * 取得所有帳號列表
     * 
     * @return 帳號列表
     */
    List<AdminUserRes> getAllAdminUsers();

    /**
     * 取得指定店家的所有帳號
     * 
     * @param storeId 店家 ID
     * @return 帳號列表
     */
    List<AdminUserRes> getAdminUsersByStore(String storeId);

    /**
     * 啟用帳號
     * 
     * @param userId 帳號 ID
     */
    void activateAdminUser(String userId);

    /**
     * 停用帳號
     * 
     * <p>停用後帳號無法登入</p>
     * 
     * @param userId 帳號 ID
     */
    void deactivateAdminUser(String userId);

    /**
     * 重設帳號密碼
     * 
     * <p>由 Admin 重設密碼，並設定 force_change_password = true</p>
     * 
     * @param userId 帳號 ID
     * @return 新的初始密碼
     */
    String resetPassword(String userId);

    /**
     * 刪除帳號（軟刪除或停用）
     * 
     * @param userId 帳號 ID
     */
    void deleteAdminUser(String userId);
}
