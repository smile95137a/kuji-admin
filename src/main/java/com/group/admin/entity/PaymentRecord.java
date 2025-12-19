package com.group.admin.entity;

import lombok.Data;

@Data
public class PaymentRecord {
    private Long id;
    private java.math.BigDecimal amount;
    private java.time.LocalDateTime createDate;
    private String currencyType;
    private String paymentMethod;
    private String status;
    private java.time.LocalDateTime transactionDate;
    private String transactionId;
    private java.time.LocalDateTime updateDate;
    private Long userId;
}
