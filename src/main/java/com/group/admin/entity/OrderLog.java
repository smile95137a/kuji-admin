package com.group.admin.entity;

import lombok.Data;

@Data
public class OrderLog {
    private Long id;
    private java.time.LocalDateTime changeTime;
    private String newStatus;
    private String oldStatus;
    private Long orderId;
}
