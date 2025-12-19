package com.group.admin.entity;

import lombok.Data;

@Data
public class DrawResult {
    private Long drawId;
    private java.math.BigDecimal amount;
    private java.time.LocalDateTime createDate;
    private Integer drawCount;
    private java.time.LocalDateTime drawTime;
    private String prizeNumber;
    private Long productDetailId;
    private Long productId;
    private Integer remainingDrawCount;
    private Long remainingTime;
    private String status;
    private Long totalDrawCount;
    private java.time.LocalDateTime updateDate;
    private Long userId;
    private String imageUrls;
    private String productName;
    private java.math.BigDecimal bonusPrice;
    private java.math.BigDecimal price;
    private java.math.BigDecimal sliverPrice;
    private String payType;
    private java.time.LocalDateTime endTimes;
    private String level;
}
