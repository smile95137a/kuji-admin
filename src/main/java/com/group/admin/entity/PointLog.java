package com.group.admin.entity;

import lombok.Data;

@Data
public class PointLog {
    private String id;
    private String userId;
    private String pointType;
    private String operationType;
    private Long amount;
    private Long beforeBalance;
    private Long afterBalance;
    private String referenceType;
    private String referenceId;
    private String remark;
    private java.time.LocalDateTime expireAt;
    private java.time.LocalDateTime createdAt;
}
