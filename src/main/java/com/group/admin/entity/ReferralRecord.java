package com.group.admin.entity;

import java.time.LocalDateTime;

public class ReferralRecord {
    private String id;

    private String referralCodeId;

    private String referralCode;
    
    private String userId;
    
    private String usedCode;
    
    private String storeId;

    private String referrerId;

    private String refereeId;

    private String refereeUsername;

    private Long rewardGold;

    private Long rewardBonus;

    private Boolean isRewardGiven;

    private LocalDateTime rewardGivenAt;
    
    private LocalDateTime referredAt;

    private LocalDateTime createdAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getReferralCodeId() {
        return referralCodeId;
    }

    public void setReferralCodeId(String referralCodeId) {
        this.referralCodeId = referralCodeId == null ? null : referralCodeId.trim();
    }

    public String getReferralCode() {
        return referralCode;
    }

    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode == null ? null : referralCode.trim();
    }
    
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId == null ? null : userId.trim();
    }
    
    public String getUsedCode() {
        return usedCode;
    }

    public void setUsedCode(String usedCode) {
        this.usedCode = usedCode == null ? null : usedCode.trim();
    }
    
    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId == null ? null : storeId.trim();
    }

    public String getReferrerId() {
        return referrerId;
    }

    public void setReferrerId(String referrerId) {
        this.referrerId = referrerId == null ? null : referrerId.trim();
    }

    public String getRefereeId() {
        return refereeId;
    }

    public void setRefereeId(String refereeId) {
        this.refereeId = refereeId == null ? null : refereeId.trim();
    }

    public String getRefereeUsername() {
        return refereeUsername;
    }

    public void setRefereeUsername(String refereeUsername) {
        this.refereeUsername = refereeUsername == null ? null : refereeUsername.trim();
    }

    public Long getRewardGold() {
        return rewardGold;
    }

    public void setRewardGold(Long rewardGold) {
        this.rewardGold = rewardGold;
    }

    public Long getRewardBonus() {
        return rewardBonus;
    }

    public void setRewardBonus(Long rewardBonus) {
        this.rewardBonus = rewardBonus;
    }

    public Boolean getIsRewardGiven() {
        return isRewardGiven;
    }

    public void setIsRewardGiven(Boolean isRewardGiven) {
        this.isRewardGiven = isRewardGiven;
    }

    public LocalDateTime getRewardGivenAt() {
        return rewardGivenAt;
    }

    public void setRewardGivenAt(LocalDateTime rewardGivenAt) {
        this.rewardGivenAt = rewardGivenAt;
    }
    
    public LocalDateTime getReferredAt() {
        return referredAt;
    }

    public void setReferredAt(LocalDateTime referredAt) {
        this.referredAt = referredAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}