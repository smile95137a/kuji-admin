package com.group.admin.entity;

import lombok.Data;

@Data
public class Order {
    private String id;
    private String userId;
    private Long amount;
    private String type;
    private String paymentProvider;
    private String providerTradeNo;
    private String status;
    private java.time.LocalDateTime createDate;
    private java.time.LocalDateTime updateDate;
}
