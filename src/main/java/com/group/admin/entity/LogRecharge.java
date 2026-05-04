package com.group.admin.entity;

import java.time.LocalDateTime;

public class LogRecharge {
    private String id;

    private String userId;

    private String rechargeId;

    private String planId;

    private String planName;

    private Long amount;

    private Long goldAdded;

    private Long bonusAdded;

    private String paymentMethod;

    private String paymentGatewayRef;

    private String result;

    private String errorMessage;

    private String ip;

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

    public String getRechargeId() {
        return rechargeId;
    }

    public void setRechargeId(String rechargeId) {
        this.rechargeId = rechargeId == null ? null : rechargeId.trim();
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId == null ? null : planId.trim();
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName == null ? null : planName.trim();
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Long getGoldAdded() {
        return goldAdded;
    }

    public void setGoldAdded(Long goldAdded) {
        this.goldAdded = goldAdded;
    }

    public Long getBonusAdded() {
        return bonusAdded;
    }

    public void setBonusAdded(Long bonusAdded) {
        this.bonusAdded = bonusAdded;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod == null ? null : paymentMethod.trim();
    }

    public String getPaymentGatewayRef() {
        return paymentGatewayRef;
    }

    public void setPaymentGatewayRef(String paymentGatewayRef) {
        this.paymentGatewayRef = paymentGatewayRef == null ? null : paymentGatewayRef.trim();
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

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip == null ? null : ip.trim();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
