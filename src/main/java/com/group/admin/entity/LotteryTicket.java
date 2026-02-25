package com.group.admin.entity;

import java.time.LocalDateTime;

public class LotteryTicket {
    private String id;

    private String lotteryId;

    private Integer ticketNumber;

    /** 刮刮樂專用：刮開後揭露的號碼；一番賞/扭蛋為 null */
    private Integer revealedNumber;

    private String prizeId;

    private String prizeLevel;

    private String status;

    private String drawnBy;

    private LocalDateTime drawnAt;

    private Byte isDesignatedPrize;

    private String designatedBy;

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

    public Integer getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(Integer ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public Integer getRevealedNumber() {
        return revealedNumber;
    }

    public void setRevealedNumber(Integer revealedNumber) {
        this.revealedNumber = revealedNumber;
    }

    public String getPrizeId() {
        return prizeId;
    }

    public void setPrizeId(String prizeId) {
        this.prizeId = prizeId == null ? null : prizeId.trim();
    }

    public String getPrizeLevel() {
        return prizeLevel;
    }

    public void setPrizeLevel(String prizeLevel) {
        this.prizeLevel = prizeLevel == null ? null : prizeLevel.trim();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public String getDrawnBy() {
        return drawnBy;
    }

    public void setDrawnBy(String drawnBy) {
        this.drawnBy = drawnBy == null ? null : drawnBy.trim();
    }

    public LocalDateTime getDrawnAt() {
        return drawnAt;
    }

    public void setDrawnAt(LocalDateTime drawnAt) {
        this.drawnAt = drawnAt;
    }

    public Byte getIsDesignatedPrize() {
        return isDesignatedPrize;
    }

    public void setIsDesignatedPrize(Byte isDesignatedPrize) {
        this.isDesignatedPrize = isDesignatedPrize;
    }

    public String getDesignatedBy() {
        return designatedBy;
    }

    public void setDesignatedBy(String designatedBy) {
        this.designatedBy = designatedBy == null ? null : designatedBy.trim();
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