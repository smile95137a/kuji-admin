package com.group.admin.entity;

import lombok.Data;

@Data
public class LotteryDrawRecord {
    private String id;
    private String lotteryId;
    private String userId;
    private String prizeId;
    private String selectedNumber;
    private String costType;
    private Long costAmount;
    private String status;
    private java.time.LocalDateTime createdAt;
}
