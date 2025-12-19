package com.group.admin.entity;

import lombok.Data;

@Data
public class UserUpdateLog {
    private Long id;
    private String userIds;
    private java.math.BigDecimal sliverCoinDelta;
    private java.math.BigDecimal bonusDelta;
    private java.time.LocalDateTime updateTime;
    private String operator;
    private java.time.LocalDateTime createdAt;
    private java.math.BigDecimal balance;
}
