package com.group.admin.entity;

import lombok.Data;

@Data
public class PasswordResetTokens {
    private Long id;
    private java.time.LocalDateTime createTime;
    private java.time.LocalDateTime expireTime;
    private Boolean isActive;
    private Boolean passwordChanged;
    private String token;
    private java.time.LocalDateTime updateTime;
    private Long userId;
}
