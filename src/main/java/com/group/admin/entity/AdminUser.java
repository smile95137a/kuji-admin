package com.group.admin.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 後台使用者
 */
@Data
public class AdminUser {

    /** 主鍵 UUID */
    private String id;

    /** 使用者帳號 */
    private String username;

    /** 使用者密碼 */
    private String password;

    /** 電子郵件 */
    private String email;

    /** 顯示名稱 */
    private String displayName;

    /** 聯絡電話 */
    private String phone;

    /** 狀態：ACTIVE / INACTIVE / LOCKED */
    private String status;

    /** 是否強制變更密碼 */
    private Boolean forceChangePassword;

    /** 最後登入時間 */
    private LocalDateTime lastLoginAt;

    /** 建立者 */
    private String createdBy;

    /** 建立時間 */
    private LocalDateTime createdAt;

    /** 更新者 */
    private String updatedBy;

    /** 更新時間 */
    private LocalDateTime updatedAt;

    /** 備註 */
    private String remark;

    /** 登入失敗次數 */
    private Integer failedLoginAttempts;

    /** 鎖定到期時間 */
    private LocalDateTime lockedUntil;
}