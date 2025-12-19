package com.group.admin.entity;

import lombok.Data;

@Data
public class VerificationToken {
    private Long id;
    private java.time.LocalDateTime expiryDate;
    private String token;
    private Long userId;
}
