package com.group.admin.entity;

import java.time.LocalDateTime;

public class AdminAuditLog {
    private String id;
    private String operatorId;
    private String operatorName;
    private String action;
    private String targetType;
    private String targetId;
    private String beforeValue;
    private String afterValue;
    private String remark;
    private String ipAddress;
    private LocalDateTime createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id == null ? null : id.trim(); }
    public String getOperatorId() { return operatorId; }
    public void setOperatorId(String operatorId) { this.operatorId = operatorId == null ? null : operatorId.trim(); }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName == null ? null : operatorName.trim(); }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action == null ? null : action.trim(); }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType == null ? null : targetType.trim(); }
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId == null ? null : targetId.trim(); }
    public String getBeforeValue() { return beforeValue; }
    public void setBeforeValue(String beforeValue) { this.beforeValue = beforeValue; }
    public String getAfterValue() { return afterValue; }
    public void setAfterValue(String afterValue) { this.afterValue = afterValue; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress == null ? null : ipAddress.trim(); }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
