package com.group.admin.entity;

import lombok.Data;

@Data
public class PrizeCart {
    private Long cartId;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
    private Long userId;
    private String userUid;
}
