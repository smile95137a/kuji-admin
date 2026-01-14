package com.group.admin.service;

import com.group.admin.entity.SystemLog;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系統日誌服務介面
 */
public interface SystemLogService {
    
    /**
     * 記錄操作日誌
     */
    void log(String logType, String action, String userId, String userName, 
             String requestData, String responseData, String errorMessage);
    
    /**
     * 記錄登入日誌
     */
    void logLogin(String userId, String userName, String userType, boolean success, String errorMessage);
    
    /**
     * 記錄後台管理操作
     */
    void logAdminAction(String action, String userId, String userName, Object requestData, Object responseData);
    
    /**
     * 記錄錯誤日誌
     */
    void logError(String action, String userId, String userName, String errorMessage, String stackTrace);
    
    /**
     * 查詢日誌（按類型）
     */
    List<SystemLog> getLogsByType(String logType, int limit);
    
    /**
     * 查詢日誌（按使用者）
     */
    List<SystemLog> getLogsByUserId(String userId, int limit);
    
    /**
     * 查詢日誌（按時間範圍）
     */
    List<SystemLog> getLogsByDateRange(LocalDateTime start, LocalDateTime end, int limit);
    
    /**
     * 清除過期日誌
     */
    int deleteOldLogs(int days);
}
