package com.group.admin.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group.admin.entity.SystemLog;
import com.group.admin.mapper.SystemLogMapper;
import com.group.admin.service.SystemLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
            
            fillRequestInfo(systemLog);
            systemLogMapper.insert(systemLog);
        } catch (Exception e) {
            log.error("❌ 錯誤日誌記錄失敗: {}", e.getMessage());
        }
    }
    
    @Override
    public List<SystemLog> getLogsByType(String logType, int limit) {
        return systemLogMapper.selectByType(logType, limit);
    }
    
    @Override
    public List<SystemLog> getLogsByUserId(String userId, int limit) {
        return systemLogMapper.selectByUserId(userId, limit);
    }
    
    @Override
    public List<SystemLog> getLogsByDateRange(LocalDateTime start, LocalDateTime end, int limit) {
        // 使用 selectByTypeAndDateRange 方法，傳入 null 作為 logType 來查全部
        return systemLogMapper.selectByTypeAndDateRange(null, start, end);
    }
    
    @Override
    public int deleteOldLogs(int days) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        int deleted = systemLogMapper.deleteOldLogs(cutoff);
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
        
        // 填入 HTTP 請求資訊
        fillRequestInfo(systemLog);
        
        return systemLog;
    }
    
    private void fillRequestInfo(SystemLog systemLog) {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                systemLog.setRequestIp(getClientIp(request));
                systemLog.setRequestUrl(request.getRequestURI());
                systemLog.setRequestMethod(request.getMethod());
            }
        } catch (Exception e) {
            log.debug("無法取得 HTTP 請求資訊: {}", e.getMessage());
        }
    }
    
    private String determineUserType(String userName) {
        if (userName == null) return "SYSTEM";
        if (userName.contains("@")) return "ADMIN";
        return "USER";
    }
    
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多個代理時取第一個
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
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
