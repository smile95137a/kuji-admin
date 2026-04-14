package com.group.admin.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group.admin.entity.SystemLog;
import com.group.admin.mapper.SystemLogMapper;
import com.group.admin.repository.SystemLogRepository;
import com.group.admin.service.SystemLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 系統日誌服務實作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemLogServiceImpl implements SystemLogService {
    
    private final SystemLogMapper systemLogMapper;
    private final SystemLogRepository systemLogRepository;
    private final ObjectMapper objectMapper;
    
    @Override
    @Async
    public void log(String logType, String action, String userId, String userName,
                    String requestData, String responseData, String errorMessage) {
        try {
            SystemLog systemLog = buildLog(logType, action, userId, userName, requestData, responseData, errorMessage);
            systemLogMapper.insert(systemLog);
            log.debug("📝 日誌已記錄: type={}, action={}, user={}", logType, action, userName);
        } catch (Exception e) {
            log.error("❌ 日誌記錄失敗: {}", e.getMessage());
        }
    }
    
    @Override
    @Async
    public void logLogin(String userId, String userName, String userType, boolean success, String errorMessage) {
        String action = success ? "LOGIN_SUCCESS" : "LOGIN_FAILED";
        String logType = "AUTH";
        
        String requestData = String.format("{\"userName\":\"%s\",\"userType\":\"%s\",\"success\":%s}", 
            userName, userType, success);
        
        log(logType, action, userId, userName, requestData, null, success ? null : errorMessage);
        
        if (success) {
            log.info("✅ 登入成功: user={}, type={}", userName, userType);
        } else {
            log.warn("❌ 登入失敗: user={}, type={}, error={}", userName, userType, errorMessage);
        }
    }
    
    @Override
    @Async
    public void logAdminAction(String action, String userId, String userName, Object requestData, Object responseData) {
        String logType = "SYSTEM";
        String requestJson = toJson(requestData);
        String responseJson = toJson(responseData);
        
        log(logType, action, userId, userName, requestJson, responseJson, null);
    }
    
    @Override
    @Async
    public void logError(String action, String userId, String userName, String errorMessage, String stackTrace) {
        String logType = "ERROR";
        
        try {
            SystemLog systemLog = new SystemLog();
            systemLog.setId(UUID.randomUUID().toString());
            systemLog.setLogType(logType);
            systemLog.setAction(action);
            systemLog.setUserId(userId);
            systemLog.setUserType(userName != null && userName.contains("@") ? "ADMIN" : "USER");
            systemLog.setErrorMessage(errorMessage);
            systemLog.setErrorStack(stackTrace != null ? 
                stackTrace.substring(0, Math.min(stackTrace.length(), 4000)) : null);
            systemLog.setCreatedAt(LocalDateTime.now());
            
            systemLogMapper.insert(systemLog);
        } catch (Exception e) {
            log.error("❌ 錯誤日誌記錄失敗: {}", e.getMessage());
        }
    }
    
    @Override
    public List<SystemLog> getLogsByType(String logType, int limit) {
        return systemLogRepository.selectByType(logType, limit);
    }
    
    @Override
    public List<SystemLog> getLogsByUserId(String userId, int limit) {
        return systemLogRepository.selectByUserId(userId, limit);
    }
    
    @Override
    public List<SystemLog> getLogsByDateRange(LocalDateTime start, LocalDateTime end, int limit) {
        // 使用 selectByTypeAndDateRange 方法，傳入 null 作為 logType 來查全部
        return systemLogRepository.selectByTypeAndDateRange(null, start, end);
    }
    
    @Override
    public int deleteOldLogs(int days) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        int deleted = systemLogRepository.deleteOldLogs(cutoff);
        log.info("🗑️ 已清除 {} 筆過期日誌（{}天前）", deleted, days);
        return deleted;
    }
    
    private SystemLog buildLog(String logType, String action, String userId, String userName,
                                String requestData, String responseData, String errorMessage) {
        SystemLog systemLog = new SystemLog();
        systemLog.setId(UUID.randomUUID().toString());
        systemLog.setLogType(logType);
        systemLog.setAction(action);
        systemLog.setUserId(userId);
        systemLog.setUserType(determineUserType(userName));
        systemLog.setRequestParams(requestData);
        systemLog.setResponseBody(responseData);
        systemLog.setErrorMessage(errorMessage);
        systemLog.setCreatedAt(LocalDateTime.now());
        
        return systemLog;
    }
    
    private String determineUserType(String userName) {
        if (userName == null) return "SYSTEM";
        if (userName.contains("@")) return "ADMIN";
        return "USER";
    }
    
    private String toJson(Object obj) {
        if (obj == null) return null;
        if (obj instanceof String) return (String) obj;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }
}
