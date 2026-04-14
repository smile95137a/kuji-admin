package com.group.admin.service;

import com.group.admin.req.AuthGoogleReq;
import com.group.admin.req.AuthLoginReq;
import com.group.admin.req.AuthRegisterReq;
import com.group.admin.entity.User;
import com.group.admin.res.AuthRes;

/**
 * 前台使用者服務介面
 *
 * @author KUJI System
 * @since 1.0.0
 */
public interface UserService {
    
    /**
     * 使用者註冊（provider = EMAIL）
     */
    User register(AuthRegisterReq req);
    
    /**
     * 使用者登入
     */
    AuthRes login(AuthLoginReq req);
    
    /**
     * Google OAuth 登入（provider = GOOGLE）
     */
    AuthRes loginWithGoogle(AuthGoogleReq req);
    
    /**
     * 根據 Email 查詢使用者
     */
    User findByEmail(String email);
    
    /**
     * 根據 ID 查詢使用者（UUID 字串）
     */
    User findById(String id);
    
    /**
     * 請求重設密碼（發送郵件）
     */
    void requestPasswordReset(String email);
    
    /**
     * OAuth 新用戶登入後補上推薦碼（一次性，已綁定則拋例外）
     */
    void applyReferral(String userId, String code);

    /**
     * 驗證重設 token 並重設密碼
     */

    void resetPassword(String token, String newPassword);

    /**
     * 更新使用者資料
     */
    void updateUser(User user);
}
