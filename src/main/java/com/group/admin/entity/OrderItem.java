package com.group.admin.entity;

import java.time.LocalDateTime;

public class OrderItem {
    private String id;

    private String orderId;

    private String prizeBoxId;

    private String lotteryId;

    private String lotteryTitle;

    private String lotteryImageUrl;

    private String prizeId;

    private String prizeName;

    private String prizeGrade;

    private String prizeImage;

    private String prizeImageUrl;

    private String prizeLevel;

    private LocalDateTime createdAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId == null ? null : orderId.trim();
    }

    public String getPrizeBoxId() {
        return prizeBoxId;
    }

    public void setPrizeBoxId(String prizeBoxId) {
        this.prizeBoxId = prizeBoxId == null ? null : prizeBoxId.trim();
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

    public String getLotteryImageUrl() {
        return lotteryImageUrl;
    }

    public void setLotteryImageUrl(String lotteryImageUrl) {
        this.lotteryImageUrl = lotteryImageUrl == null ? null : lotteryImageUrl.trim();
    }

    public String getPrizeId() {
        return prizeId;
    }

    public void setPrizeId(String prizeId) {
        this.prizeId = prizeId == null ? null : prizeId.trim();
    }

    public String getPrizeName() {
        return prizeName;
    }

    public void setPrizeName(String prizeName) {
        this.prizeName = prizeName == null ? null : prizeName.trim();
    }

    public String getPrizeGrade() {
        return prizeGrade;
    }

    public void setPrizeGrade(String prizeGrade) {
        this.prizeGrade = prizeGrade == null ? null : prizeGrade.trim();
    }

    public String getPrizeImage() {
        return prizeImage;
    }

    public void setPrizeImage(String prizeImage) {
        this.prizeImage = prizeImage == null ? null : prizeImage.trim();
    }

    public String getPrizeImageUrl() {
        return prizeImageUrl;
    }

    public void setPrizeImageUrl(String prizeImageUrl) {
        this.prizeImageUrl = prizeImageUrl == null ? null : prizeImageUrl.trim();
    }

    public String getPrizeLevel() {
        return prizeLevel;
    }

    public void setPrizeLevel(String prizeLevel) {
        this.prizeLevel = prizeLevel == null ? null : prizeLevel.trim();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}