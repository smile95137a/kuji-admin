package com.group.admin.entity;

import java.time.LocalDateTime;

/**
 * 推薦關係紀錄實體
 * 
 * @author KUJI System
 * @since 1.0.0
 */
public class ReferralRecord {
    
    private String id;
    
    /**
     * 被推薦會員 ID
     */
    private String userId;
    
    /**
     * 推薦碼 ID
     */
    private String referralCodeId;
    
    /**
     * 推薦來源店家 ID
     */
    private String storeId;
    
    /**
     * 使用的推薦碼
     */
    private String usedCode;
    
    /**
     * 推薦發生時間（註冊時間）
     */
    private LocalDateTime referredAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId == null ? null : userId.trim();
    }

    public String getReferralCodeId() {
        return referralCodeId;
    }

    public void setReferralCodeId(String referralCodeId) {
        this.referralCodeId = referralCodeId == null ? null : referralCodeId.trim();
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId == null ? null : storeId.trim();
    }

    public String getUsedCode() {
        return usedCode;
    }

    public void setUsedCode(String usedCode) {
        this.usedCode = usedCode == null ? null : usedCode.trim();
    }

    public LocalDateTime getReferredAt() {
        return referredAt;
    }

    public void setReferredAt(LocalDateTime referredAt) {
        this.referredAt = referredAt;
    }
}
