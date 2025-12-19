package com.group.admin.entity;

import lombok.Data;

@Data
public class DailySignInRecords {
    private Long id;
    private java.math.BigDecimal rewardPoints;
    private java.time.LocalDateTime signInDate;
    private Long userId;
}
