package com.group.admin.entity;

import java.time.LocalDateTime;

public class LotteryPrize {
    private String id;

    private String lotteryId;

    private String name;

    private String imageUrl;

    private String level;

    private String prizeNumber;

    private Integer quantity;

    private Integer remaining;

    private Integer weight;

    private String prizeType;

    private Long pointValue;

    private Long recycleBonus;

    private Byte isLastPrize;

    private Byte isGrandPrize;

    private Integer orderNum;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String description;

    private String content;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl == null ? null : imageUrl.trim();
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level == null ? null : level.trim();
    }

    public String getPrizeNumber() {
        return prizeNumber;
    }

    public void setPrizeNumber(String prizeNumber) {
        this.prizeNumber = prizeNumber == null ? null : prizeNumber.trim();
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getRemaining() {
        return remaining;
    }

    public void setRemaining(Integer remaining) {
        this.remaining = remaining;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public String getPrizeType() {
        return prizeType;
    }

    public void setPrizeType(String prizeType) {
        this.prizeType = prizeType == null ? null : prizeType.trim();
    }

    public Long getPointValue() {
        return pointValue;
    }

    public void setPointValue(Long pointValue) {
        this.pointValue = pointValue;
    }

    public Byte getIsLastPrize() {
        return isLastPrize;
    }

    public void setIsLastPrize(Byte isLastPrize) {
        this.isLastPrize = isLastPrize;
    }

    public Byte getIsGrandPrize() {
        return isGrandPrize;
    }

    public void setIsGrandPrize(Byte isGrandPrize) {
        this.isGrandPrize = isGrandPrize;
    }

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content == null ? null : content.trim();
    }

    public Long getRecycleBonus() {
        return recycleBonus;
    }

    public void setRecycleBonus(Long recycleBonus) {
        this.recycleBonus = recycleBonus;
    }
}