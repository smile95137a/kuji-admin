package com.group.admin.entity;

import lombok.Data;

@Data
public class Draw {
    private Long id;
    private java.time.LocalDateTime createDate;
    private java.time.LocalDateTime drawTime;
    private String status;
    private java.math.BigDecimal totalAmount;
    private Integer totalDrawCount;
    private java.time.LocalDateTime updateDate;
    private Long userId;
}
