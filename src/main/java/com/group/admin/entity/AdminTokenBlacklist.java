package com.group.admin.entity;

import java.time.LocalDateTime;

public class AdminTokenBlacklist {
    private String adminUserId;

    private Integer blacklistGen;

    private LocalDateTime updatedAt;

    public String getAdminUserId() {
        return adminUserId;
    }

    public void setAdminUserId(String adminUserId) {
        this.adminUserId = adminUserId == null ? null : adminUserId.trim();
    }

    public Integer getBlacklistGen() {
        return blacklistGen;
    }

    public void setBlacklistGen(Integer blacklistGen) {
        this.blacklistGen = blacklistGen;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
