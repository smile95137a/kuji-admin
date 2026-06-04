package com.group.admin.res.systemlog;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class AdminSystemLogItemRes {
    String id;
    String logType;
    String userId;
    String userType;
    String email;
    String loginMethod;
    String adminId;
    String adminEmail;
    String adminRole;
    String targetType;
    String targetId;
    String targetName;
    String targetNo;
    String action;
    String actorType;
    String actorId;
    String actorName;
    String orderId;
    String rechargeId;
    String walletTransactionId;
    String externalProvider;
    String externalRef;
    String paymentMethod;
    Long amount;
    String beforeStatus;
    String afterStatus;
    String callbackSummary;
    String rawPayloadHash;
    String beforeSnapshot;
    String afterSnapshot;
    String result;
    String errorMessage;
    String ip;
    String userAgent;
    LocalDateTime createdAt;
}
