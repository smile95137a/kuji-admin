package com.group.admin.entity;

import lombok.Data;

@Data
public class UserPasswordResetLog {
    private Long id;
    private java.time.LocalDateTime resetTime;
    private String resetToken;
    private java.time.LocalDateTime tokenExpiry;
    private Long userId;
}
