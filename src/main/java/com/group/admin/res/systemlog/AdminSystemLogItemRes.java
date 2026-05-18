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
    String action;
    String beforeSnapshot;
    String afterSnapshot;
    String result;
    String errorMessage;
    String ip;
    String userAgent;
    LocalDateTime createdAt;
}
