package com.group.admin.entity;

import java.time.LocalDateTime;

public class LotteryDrawRecord {
    private String id;

    private String lotteryId;

    private String userId;

    private String prizeId;

    private String ticketId;

    private String sessionId;

    private Byte isOpenerDraw;

    private Byte triggeredFreeDraw;

    private String selectedNumber;

    private String costType;

    private Long costAmount;

    private String status;

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

    public String getPrizeId() {
        return prizeId;
    }

    public void setPrizeId(String prizeId) {
        this.prizeId = prizeId == null ? null : prizeId.trim();
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId == null ? null : ticketId.trim();
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId == null ? null : sessionId.trim();
    }

    public Byte getIsOpenerDraw() {
        return isOpenerDraw;
    }

    public void setIsOpenerDraw(Byte isOpenerDraw) {
        this.isOpenerDraw = isOpenerDraw;
    }

    public Byte getTriggeredFreeDraw() {
        return triggeredFreeDraw;
    }

    public void setTriggeredFreeDraw(Byte triggeredFreeDraw) {
        this.triggeredFreeDraw = triggeredFreeDraw;
    }

    public String getSelectedNumber() {
        return selectedNumber;
    }

    public void setSelectedNumber(String selectedNumber) {
        this.selectedNumber = selectedNumber == null ? null : selectedNumber.trim();
    }

    public String getCostType() {
        return costType;
    }

    public void setCostType(String costType) {
        this.costType = costType == null ? null : costType.trim();
    }

    public Long getCostAmount() {
        return costAmount;
    }

    public void setCostAmount(Long costAmount) {
        this.costAmount = costAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}