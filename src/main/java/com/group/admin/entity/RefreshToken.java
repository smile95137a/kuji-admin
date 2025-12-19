package com.group.admin.entity;

import lombok.Data;

@Data
public class RefreshToken {
    private String id;
    private String userType;
    private String userId;
    private String token;
    private String deviceInfo;
    private String ipAddress;
    private java.time.LocalDateTime expiresAt;
    private Integer isRevoked;
    private java.time.LocalDateTime createdAt;
}
