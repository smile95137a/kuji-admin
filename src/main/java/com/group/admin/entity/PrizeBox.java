package com.group.admin.entity;

import java.time.LocalDateTime;

public class PrizeBox {
    private String id;

    private String userId;

    private String lotteryId;

    private String prizeId;

    private String storeId;

    private String drawResultId;

    private String status;

    private Byte isRecyclable;

    /** 是否可出貨：1=可, 0=不可，預設 1 */
    private Byte isShippable;

    private Long recycleBonus;

    private LocalDateTime recycledAt;

    private LocalDateTime shippedAt;

    private String orderId;

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

    public String getLotteryId() {
        return lotteryId;
    }

    public void setLotteryId(String lotteryId) {
        this.lotteryId = lotteryId == null ? null : lotteryId.trim();
    }

    public String getPrizeId() {
        return prizeId;
    }

    public void setPrizeId(String prizeId) {
        this.prizeId = prizeId == null ? null : prizeId.trim();
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId == null ? null : storeId.trim();
    }

    public String getDrawResultId() {
        return drawResultId;
    }

    public void setDrawResultId(String drawResultId) {
        this.drawResultId = drawResultId == null ? null : drawResultId.trim();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public Byte getIsRecyclable() {
        return isRecyclable;
    }

    public void setIsRecyclable(Byte isRecyclable) {
        this.isRecyclable = isRecyclable;
    }

    public Byte getIsShippable() {
        return isShippable;
    }

    public void setIsShippable(Byte isShippable) {
        this.isShippable = isShippable;
    }

    public Long getRecycleBonus() {
        return recycleBonus;
    }

    public void setRecycleBonus(Long recycleBonus) {
        this.recycleBonus = recycleBonus;
    }

    public LocalDateTime getRecycledAt() {
        return recycledAt;
    }

    public void setRecycledAt(LocalDateTime recycledAt) {
        this.recycledAt = recycledAt;
    }

    public LocalDateTime getShippedAt() {
        return shippedAt;
    }

    public void setShippedAt(LocalDateTime shippedAt) {
        this.shippedAt = shippedAt;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId == null ? null : orderId.trim();
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