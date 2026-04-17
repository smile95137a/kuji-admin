package com.group.admin.service;

public interface AuditLogService {
    void log(String operatorId, String operatorName, String action,
             String targetType, String targetId,
             String beforeValue, String afterValue,
             String remark, String ipAddress);
}
