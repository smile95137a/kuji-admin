package com.group.admin.entity;

import lombok.Data;

@Data
public class UserTransaction {
    private Long id;
    private java.math.BigDecimal amount;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime transactionDate;
    private String transactionType;
    private java.time.LocalDateTime updatedAt;
    private Long userId;
    private String userUuid;
    private String orderId;
    private String orderNumber;
    private String status;
    private String payMethod;
    private String type;
}
