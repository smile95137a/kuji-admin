package com.group.admin.service;

import com.group.admin.req.admin.ChangePasswordReq;
import com.group.admin.req.admin.CreateStoreEditorReq;
import com.group.admin.req.admin.CreateStoreOwnerReq;
import com.group.admin.req.admin.UpdateAdminUserReq;
import com.group.admin.res.admin.AdminUserRes;

import java.util.List;

public interface AdminUserService {

    AdminUserRes createStoreOwner(CreateStoreOwnerReq req);

    AdminUserRes createStoreEditor(CreateStoreEditorReq req);

    AdminUserRes getAdminUser(String userId);

    List<AdminUserRes> getAllAdminUsers();

    List<AdminUserRes> getAdminUsersByStore(String storeId);

    void activateAdminUser(String userId);

    void deactivateAdminUser(String userId);

    String resetPassword(String userId);

    void deleteAdminUser(String userId);

    // ========== 013-store-account-mgmt new methods ==========

    /**
     * 更新後台使用者資料
     */
    AdminUserRes updateAdminUser(String userId, UpdateAdminUserReq req, String operatorId);

    /**
     * 修改密碼（驗證舊密碼）
     */
    void changePassword(String userId, ChangePasswordReq req);

    /**
     * 列出後台使用者（Admin 看全部；STORE_OWNER 只看自己店家的人）
     */
    List<AdminUserRes> listAdminUsers(String storeId);

    /**
     * 停用帳號（由操作者指定）
     */
    void disableAdminUser(String userId, String operatorId);
}
