package com.group.admin.entity;

import lombok.Data;

@Data
public class RedemptionCodes {
    private Long id;
    private String code;
    private Boolean isRedeemed;
    private java.time.LocalDateTime redeemedAt;
    private Long userId;
    private Long productId;
    private String productName;
}
