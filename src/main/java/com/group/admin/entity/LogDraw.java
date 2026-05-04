package com.group.admin.entity;

import java.time.LocalDateTime;

public class LogDraw {
    private String id;

    private String userId;

    private String lotteryId;

    private String lotteryTitle;

    private String category;

    private String playMode;

    private String gameMode;

    private String ticketId;

    private Integer ticketNumber;

    private String prizeLevel;

    private String prizeName;

    private Byte isGrandPrize;

    private Long deductedGold;

    private Long deductedBonus;

    private String result;

    private String errorMessage;

    private Integer durationMs;

    private LocalDateTime createdAt;

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

    public String getLotteryId() {
        return lotteryId;
    }

    public void setLotteryId(String lotteryId) {
        this.lotteryId = lotteryId == null ? null : lotteryId.trim();
    }

    public String getLotteryTitle() {
        return lotteryTitle;
    }

    public void setLotteryTitle(String lotteryTitle) {
        this.lotteryTitle = lotteryTitle == null ? null : lotteryTitle.trim();
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category == null ? null : category.trim();
    }

    public String getPlayMode() {
        return playMode;
    }

    public void setPlayMode(String playMode) {
        this.playMode = playMode == null ? null : playMode.trim();
    }

    public String getGameMode() {
        return gameMode;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode == null ? null : gameMode.trim();
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId == null ? null : ticketId.trim();
    }

    public Integer getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(Integer ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public String getPrizeLevel() {
        return prizeLevel;
    }

    public void setPrizeLevel(String prizeLevel) {
        this.prizeLevel = prizeLevel == null ? null : prizeLevel.trim();
    }

    public String getPrizeName() {
        return prizeName;
    }

    public void setPrizeName(String prizeName) {
        this.prizeName = prizeName == null ? null : prizeName.trim();
    }

    public Byte getIsGrandPrize() {
        return isGrandPrize;
    }

    public void setIsGrandPrize(Byte isGrandPrize) {
        this.isGrandPrize = isGrandPrize;
    }

    public Long getDeductedGold() {
        return deductedGold;
    }

    public void setDeductedGold(Long deductedGold) {
        this.deductedGold = deductedGold;
    }

    public Long getDeductedBonus() {
        return deductedBonus;
    }

    public void setDeductedBonus(Long deductedBonus) {
        this.deductedBonus = deductedBonus;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result == null ? null : result.trim();
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage == null ? null : errorMessage.trim();
    }

    public Integer getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Integer durationMs) {
        this.durationMs = durationMs;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
