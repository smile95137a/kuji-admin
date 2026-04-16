package com.group.admin.entity;

import java.time.LocalDateTime;

public class UserWallet {
    private String id;

    private String userId;

    private Long goldCoins;

    private Long bonusCoins;

    private Long totalRecharged;

    private Integer version;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

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

    public Long getGoldCoins() {
        return goldCoins;
    }

    public void setGoldCoins(Long goldCoins) {
        this.goldCoins = goldCoins;
    }

    public Long getBonusCoins() {
        return bonusCoins;
    }

    public void setBonusCoins(Long bonusCoins) {
        this.bonusCoins = bonusCoins;
    }

    public Long getTotalRecharged() {
        return totalRecharged;
    }

    public void setTotalRecharged(Long totalRecharged) {
        this.totalRecharged = totalRecharged;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
