package com.group.admin.entity;

import lombok.Data;

@Data
public class LotteryPrize {
    private String id;
    private String lotteryId;
    private String name;
    private String description;
    private String imageUrl;
    private String level;
    private String prizeNumber;
    private Integer quantity;
    private Integer remaining;
    private Integer weight;
    private String prizeType;
    private Long pointValue;
    private Integer isLastPrize;
    private Integer isGrandPrize;
    private Integer orderNum;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
}
