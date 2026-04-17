package com.group.admin.service.impl;

import com.group.admin.entity.AdminAuditLog;
import com.group.admin.mapper.AdminAuditLogMapper;
import com.group.admin.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AdminAuditLogMapper adminAuditLogMapper;

    @Override
    public void log(String operatorId, String operatorName, String action,
                    String targetType, String targetId,
                    String beforeValue, String afterValue,
                    String remark, String ipAddress) {
        try {
            AdminAuditLog auditLog = new AdminAuditLog();
            auditLog.setId(UUID.randomUUID().toString());
            auditLog.setOperatorId(operatorId);
            auditLog.setOperatorName(operatorName);
            auditLog.setAction(action);
            auditLog.setTargetType(targetType);
            auditLog.setTargetId(targetId);
            auditLog.setBeforeValue(beforeValue);
            auditLog.setAfterValue(afterValue);
            auditLog.setRemark(remark);
            auditLog.setIpAddress(ipAddress);
            auditLog.setCreatedAt(LocalDateTime.now());
            adminAuditLogMapper.insertSelective(auditLog);
        } catch (Exception e) {
            log.warn("⚠️ 操作日誌寫入失敗: {}", e.getMessage());
        }
    }
}
