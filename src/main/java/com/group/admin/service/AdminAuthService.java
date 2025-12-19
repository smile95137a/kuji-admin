package com.group.admin.service;

import com.group.admin.req.auth.AdminLoginReq;
import com.group.admin.req.auth.ChangePasswordReq;
import com.group.admin.req.auth.RefreshTokenReq;
import com.group.admin.res.auth.LoginRes;

/**
 * 後台認證服務介面
 * 
 * <p>處理後台帳號的登入、登出、Token 管理</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
public interface AdminAuthService {

    /**
     * 後台登入
     * 
     * <p>驗證帳號密碼，成功後回傳 Token</p>
     * 
     * @param req 登入請求
     * @return 登入回應（包含 Token 與使用者資訊）
     */
    LoginRes login(AdminLoginReq req);

    /**
     * 首次登入修改密碼
     * 
     * <p>依據 store-account-management.prompt.md：
     * 首次登入必須修改密碼才能正常使用系統</p>
     * 
     * @param req 修改密碼請求
     * @return 新的登入回應（包含新 Token）
     */
    LoginRes firstLoginChangePassword(ChangePasswordReq req);

    /**
     * 一般修改密碼
     * 
     * @param req 修改密碼請求
     */
    void changePassword(ChangePasswordReq req);

    /**
     * 刷新 Token
     * 
     * @param req 刷新 Token 請求
     * @return 新的登入回應
     */
    LoginRes refreshToken(RefreshTokenReq req);

    /**
     * 登出
     * 
     * <p>撤銷當前 Token</p>
     */
    void logout();

    /**
     * 取得當前登入使用者 ID
     * 
     * @return 使用者 ID (UUID 字串)
     */
    String getCurrentUserId();

    /**
     * 取得當前登入使用者帳號
     * 
     * @return 使用者帳號
     */
    String getCurrentUsername();
}
