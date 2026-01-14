package com.group.admin.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系統操作日誌
 * 記錄所有重要的系統操作、交易、錯誤等
 */
@Data
public class SystemLog {
    private String id;
    
    /**
     * 日誌類型: AUTH/TRANSACTION/ORDER/ERROR/EMAIL/SYSTEM
     */
    private String logType;
    
    /**
     * 操作行為
     */
    private String action;
    
    /**
     * 操作用戶ID
     */
    private String userId;
    
    /**
     * 用戶類型: USER/ADMIN/SYSTEM
     */
    private String userType;
    
    /**
     * 目標類型
     */
    private String targetType;
    
    /**
     * 目標ID
     */
    private String targetId;
    
    /**
     * 請求IP
     */
    private String requestIp;
    
    /**
     * 請求URL
     */
    private String requestUrl;
    
    /**
     * 請求方法
     */
    private String requestMethod;
    
    /**
     * 請求參數（JSON）
     */
    private String requestParams;
    
    /**
     * 回應狀態碼
     */
    private Integer responseStatus;
    
    /**
     * 回應內容（摘要）
     */
    private String responseBody;
    
    /**
     * 錯誤訊息
     */
    private String errorMessage;
    
    /**
     * 錯誤堆疊
     */
    private String errorStack;
    
    /**
     * 執行時間（毫秒）
     */
    private Long durationMs;
    
    /**
     * 額外資料（JSON）
     */
    private String extraData;
    
    private LocalDateTime createdAt;
}
