package com.group.admin.entity;

import lombok.Data;

@Data
public class AdminUserRole {
    private String id;
    private String adminUserId;
    private String roleId;
    private java.time.LocalDateTime createdAt;
}
