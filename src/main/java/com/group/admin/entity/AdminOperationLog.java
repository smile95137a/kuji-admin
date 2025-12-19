package com.group.admin.entity;

import lombok.Data;

@Data
public class AdminOperationLog {
    private String id;
    private String adminId;
    private String operationType;
    private String targetType;
    private String targetId;
    private String description;
    private String ipAddress;
    private String userAgent;
    private java.time.LocalDateTime createdAt;
}
