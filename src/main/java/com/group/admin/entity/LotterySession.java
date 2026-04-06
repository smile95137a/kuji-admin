package com.group.admin.entity;

import java.time.LocalDateTime;

public class LotterySession {
    private String id;

    private String lotteryId;

    private String openerUserId;

    private Integer protectionDraws;

    private LocalDateTime protectionStartTime;

    private LocalDateTime protectionEndTime;

    private Integer openerDrawCount;

    private Long openerTotalCost;

    private Byte freeDrawEnabled;

    private Byte freeDrawTriggered;

    private Long freeDrawRefundAmount;

    private LocalDateTime freeDrawTriggeredAt;

    private String freeDrawPrizeId;

    private String playerDesignatedNumbers;

    private LocalDateTime designationDeadline;

    private String status;

    private LocalDateTime completedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

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

    public String getOpenerUserId() {
        return openerUserId;
    }

    public void setOpenerUserId(String openerUserId) {
        this.openerUserId = openerUserId == null ? null : openerUserId.trim();
    }

    public Integer getProtectionDraws() {
        return protectionDraws;
    }

    public void setProtectionDraws(Integer protectionDraws) {
        this.protectionDraws = protectionDraws;
    }

    public LocalDateTime getProtectionStartTime() {
        return protectionStartTime;
    }

    public void setProtectionStartTime(LocalDateTime protectionStartTime) {
        this.protectionStartTime = protectionStartTime;
    }

    public LocalDateTime getProtectionEndTime() {
        return protectionEndTime;
    }

    public void setProtectionEndTime(LocalDateTime protectionEndTime) {
        this.protectionEndTime = protectionEndTime;
    }

    public Integer getOpenerDrawCount() {
        return openerDrawCount;
    }

    public void setOpenerDrawCount(Integer openerDrawCount) {
        this.openerDrawCount = openerDrawCount;
    }

    public Long getOpenerTotalCost() {
        return openerTotalCost;
    }

    public void setOpenerTotalCost(Long openerTotalCost) {
        this.openerTotalCost = openerTotalCost;
    }

    public Byte getFreeDrawEnabled() {
        return freeDrawEnabled;
    }

    public void setFreeDrawEnabled(Byte freeDrawEnabled) {
        this.freeDrawEnabled = freeDrawEnabled;
    }

    public Byte getFreeDrawTriggered() {
        return freeDrawTriggered;
    }

    public void setFreeDrawTriggered(Byte freeDrawTriggered) {
        this.freeDrawTriggered = freeDrawTriggered;
    }

    public Long getFreeDrawRefundAmount() {
        return freeDrawRefundAmount;
    }

    public void setFreeDrawRefundAmount(Long freeDrawRefundAmount) {
        this.freeDrawRefundAmount = freeDrawRefundAmount;
    }

    public LocalDateTime getFreeDrawTriggeredAt() {
        return freeDrawTriggeredAt;
    }

    public void setFreeDrawTriggeredAt(LocalDateTime freeDrawTriggeredAt) {
        this.freeDrawTriggeredAt = freeDrawTriggeredAt;
    }

    public String getFreeDrawPrizeId() {
        return freeDrawPrizeId;
    }

    public void setFreeDrawPrizeId(String freeDrawPrizeId) {
        this.freeDrawPrizeId = freeDrawPrizeId == null ? null : freeDrawPrizeId.trim();
    }

    public String getPlayerDesignatedNumbers() {
        return playerDesignatedNumbers;
    }

    public void setPlayerDesignatedNumbers(String playerDesignatedNumbers) {
        this.playerDesignatedNumbers = playerDesignatedNumbers == null ? null : playerDesignatedNumbers.trim();
    }

    public LocalDateTime getDesignationDeadline() {
        return designationDeadline;
    }

    public void setDesignationDeadline(LocalDateTime designationDeadline) {
        this.designationDeadline = designationDeadline;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
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