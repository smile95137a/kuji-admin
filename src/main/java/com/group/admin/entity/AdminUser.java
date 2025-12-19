package com.group.admin.entity;

import lombok.Data;

@Data
public class AdminUser {
    private String id;
    private String username;
    private String password;
    private String email;
    private String displayName;
    private String phone;
    private String status;
    private Boolean forceChangePassword;
    private java.time.LocalDateTime lastLoginAt;
    private String createdBy;
    private java.time.LocalDateTime createdAt;
    private String updatedBy;
    private java.time.LocalDateTime updatedAt;
    private String remark;
}
