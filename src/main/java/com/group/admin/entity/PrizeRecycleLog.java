package com.group.admin.entity;

import lombok.Data;

@Data
public class PrizeRecycleLog {
    private Long id;
    private Long userId;
    private Long productDetailId;
    private java.math.BigDecimal sliverCoin;
    private java.time.LocalDateTime recycleTime;
    private String operator;
}
