package com.group.admin.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 前台玩家實體
 * 對應 DDL_UUID.sql 中的 user 表
 */
@Data
public class User {
    /**
     * 使用者 ID (UUID)
     */
    private String id;
    
    /**
     * Email
     */
    private String email;
    
    /**
     * 暱稱
     */
    private String nickname;
    
    /**
     * 密碼（BCrypt 加密，OAuth 用戶可為空）
     */
    private String password;
    
    /**
     * 頭像網址
     */
    private String avatar;
    
    /**
     * 登入來源：EMAIL/GOOGLE
     */
    private String provider;
    
    /**
     * OAuth Provider 的用戶 ID
     */
    private String providerId;
    
    /**
     * 儲值金（付費購買）
     */
    private Long goldCoins;
    
    /**
     * 紅利金（系統贈送）
     */
    private Long bonusCoins;
    
    /**
     * 狀態：ACTIVE/INACTIVE
     */
    private String status;
    
    /**
     * Email 是否驗證：0=否, 1=是
     */
    private Integer emailVerified;
    
    /**
     * 手機號碼
     */
    private String phoneNumber;
    
    /**
     * 最後登入時間
     */
    private LocalDateTime lastLoginAt;
    
    /**
     * 建立時間
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新時間
     */
    private LocalDateTime updatedAt;
}
