package com.group.admin.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 郵件發送記錄
 * 記錄所有發送的郵件內容與狀態
 */
@Data
public class EmailLog {
    private String id;
    
    /**
     * 郵件類型: VERIFICATION/PASSWORD_RESET/NOTIFICATION/ORDER
     */
    private String emailType;
    
    /**
     * 收件人信箱
     */
    private String toEmail;
    
    /**
     * 收件人姓名
     */
    private String toName;
    
    /**
     * 郵件主旨
     */
    private String subject;
    
    /**
     * 郵件內容
     */
    private String content;
    
    /**
     * 使用的模板名稱
     */
    private String templateName;
    
    /**
     * 模板參數（JSON）
     */
    private String templateParams;
    
    /**
     * 狀態: PENDING/SENT/FAILED
     */
    private String status;
    
    /**
     * 發送失敗原因
     */
    private String errorMessage;
    
    /**
     * 實際發送時間
     */
    private LocalDateTime sentAt;
    
    /**
     * 重試次數
     */
    private Integer retryCount;
    
    /**
     * 關聯類型
     */
    private String relatedType;
    
    /**
     * 關聯ID
     */
    private String relatedId;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
