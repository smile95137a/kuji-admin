package com.group.admin.entity;

import java.time.LocalDateTime;

public class WalletTransaction {
    private String id;

    private String userId;

    private String transactionType;

    private String coinType;

    private Long amount;

    private Long balanceAfter;

    private String relatedId;

    private String description;

    private String createdBy;

    private LocalDateTime createdAt;

    private Long goldDelta;

    private Long bonusDelta;

    private Long goldAfter;

    private Long bonusAfter;

    private String referenceId;

    private String reason;

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

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType == null ? null : transactionType.trim();
    }

    public String getCoinType() {
        return coinType;
    }

    public void setCoinType(String coinType) {
        this.coinType = coinType == null ? null : coinType.trim();
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Long getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(Long balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public String getRelatedId() {
        return relatedId;
    }

    public void setRelatedId(String relatedId) {
        this.relatedId = relatedId == null ? null : relatedId.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy == null ? null : createdBy.trim();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getGoldDelta() {
        return goldDelta;
    }

    public void setGoldDelta(Long goldDelta) {
        this.goldDelta = goldDelta;
    }

    public Long getBonusDelta() {
        return bonusDelta;
    }

    public void setBonusDelta(Long bonusDelta) {
        this.bonusDelta = bonusDelta;
    }

    public Long getGoldAfter() {
        return goldAfter;
    }

    public void setGoldAfter(Long goldAfter) {
        this.goldAfter = goldAfter;
    }

    public Long getBonusAfter() {
        return bonusAfter;
    }

    public void setBonusAfter(Long bonusAfter) {
        this.bonusAfter = bonusAfter;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId == null ? null : referenceId.trim();
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason == null ? null : reason.trim();
    }
}