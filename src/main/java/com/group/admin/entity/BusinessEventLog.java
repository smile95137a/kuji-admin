package com.group.admin.entity;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BusinessEventLog {

    private String id;
    private String eventType;
    private String action;
    private String result;
    private String actorType;
    private String actorId;
    private String actorName;
    private String targetType;
    private String targetId;
    private String targetNo;
    private String userId;
    private String orderId;
    private String rechargeId;
    private String walletTransactionId;
    private String externalProvider;
    private String externalRef;
    private Long amount;
    private String paymentMethod;
    private String beforeStatus;
    private String afterStatus;
    private String beforeSnapshot;
    private String afterSnapshot;
    private String callbackSummary;
    private String rawPayloadHash;
    private String errorMessage;
    private String ip;
    private String userAgent;
    private LocalDateTime createdAt;
}
