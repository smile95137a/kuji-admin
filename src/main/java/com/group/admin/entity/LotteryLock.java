package com.group.admin.entity;

import lombok.Data;

@Data
public class LotteryLock {
    private String id;
    private String lotteryId;
    private String userId;
    private java.time.LocalDateTime lockStartTime;
    private java.time.LocalDateTime lockEndTime;
    private Integer isActive;
    private java.time.LocalDateTime createdAt;
}
