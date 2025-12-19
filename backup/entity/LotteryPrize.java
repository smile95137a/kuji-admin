package com.group.admin.entity;

import lombok.Data;

@Data
public class LotteryPrize {
    private String id;
    private String lotteryId;
    private String name;
    private String description;
    private Integer quantity;
    private Integer remaining;
    private Integer weight;
    private String type;
    private Long value;
    private Integer orderNum;
    private java.time.LocalDateTime createDate;
    private java.time.LocalDateTime updateDate;
}
