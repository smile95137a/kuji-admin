package com.group.admin.entity;

import java.time.LocalDateTime;

public class UserLoginHistory {
    private String id;
    private String userId;
    private String userType;
    private LocalDateTime loginTime;
    private String ipAddress;
    private String deviceInfo;
    private String loginMethod;
    private String status;
    private String failReason;
    private LocalDateTime createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id == null ? null : id.trim(); }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId == null ? null : userId.trim(); }
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType == null ? null : userType.trim(); }
    public LocalDateTime getLoginTime() { return loginTime; }
    public void setLoginTime(LocalDateTime loginTime) { this.loginTime = loginTime; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress == null ? null : ipAddress.trim(); }
    public String getDeviceInfo() { return deviceInfo; }
    public void setDeviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo == null ? null : deviceInfo.trim(); }
    public String getLoginMethod() { return loginMethod; }
    public void setLoginMethod(String loginMethod) { this.loginMethod = loginMethod == null ? null : loginMethod.trim(); }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status == null ? null : status.trim(); }
    public String getFailReason() { return failReason; }
    public void setFailReason(String failReason) { this.failReason = failReason == null ? null : failReason.trim(); }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
