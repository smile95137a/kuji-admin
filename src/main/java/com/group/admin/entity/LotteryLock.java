package com.group.admin.entity;

import java.time.LocalDateTime;

public class LotteryLock {
    private String id;

    private String lotteryId;

    private String userId;

    private LocalDateTime lockStartTime;

    private LocalDateTime lockEndTime;

    private Byte isActive;

    private LocalDateTime createdAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getLotteryId() {
        return lotteryId;
    }

    public void setLotteryId(String lotteryId) {
        this.lotteryId = lotteryId == null ? null : lotteryId.trim();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId == null ? null : userId.trim();
    }

    public LocalDateTime getLockStartTime() {
        return lockStartTime;
    }

    public void setLockStartTime(LocalDateTime lockStartTime) {
        this.lockStartTime = lockStartTime;
    }

    public LocalDateTime getLockEndTime() {
        return lockEndTime;
    }

    public void setLockEndTime(LocalDateTime lockEndTime) {
        this.lockEndTime = lockEndTime;
    }

    public Byte getIsActive() {
        return isActive;
    }

    public void setIsActive(Byte isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}