package com.group.admin.entity;

import java.time.LocalDateTime;

public class UserTokenBlacklist {
    private String userId;
    private Integer blacklistGen;
    private LocalDateTime updatedAt;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId == null ? null : userId.trim(); }
    public Integer getBlacklistGen() { return blacklistGen; }
    public void setBlacklistGen(Integer blacklistGen) { this.blacklistGen = blacklistGen; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
