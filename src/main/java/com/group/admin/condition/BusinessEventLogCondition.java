package com.group.admin.condition;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BusinessEventLogCondition {

    private String eventType;
    private String result;
    private String action;
    private String actorId;
    private String userId;
    private String orderId;
    private String rechargeId;
    private String targetId;
    private String externalRef;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
}
