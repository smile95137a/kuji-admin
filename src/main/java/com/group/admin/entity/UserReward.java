package com.group.admin.entity;

import lombok.Data;

@Data
public class UserReward {
    private Long id;
    private java.time.LocalDateTime createdAt;
    private java.math.BigDecimal rewardAmount;
    private java.time.LocalDateTime rewardDate;
    private Long userId;
    private java.math.BigDecimal thresholdAmount;
}
