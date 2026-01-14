package com.group.admin.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 跑馬燈訊息
 * 前台即時顯示的跑馬燈通知
 */
@Data
public class Marquee {
    private String id;
    
    /**
     * 跑馬燈內容
     */
    private String content;
    
    /**
     * 點擊連結
     */
    private String linkUrl;
    
    /**
     * 連結類型: INTERNAL/EXTERNAL
     */
    private String linkType;
    
    /**
     * 優先級（數字越大越優先）
     */
    private Integer priority;
    
    /**
     * 背景顏色
     */
    private String bgColor;
    
    /**
     * 文字顏色
     */
    private String textColor;
    
    /**
     * 開始顯示時間
     */
    private LocalDateTime startTime;
    
    /**
     * 結束顯示時間
     */
    private LocalDateTime endTime;
    
    /**
     * 是否啟用
     */
    private Byte isActive;
    
    /**
     * 建立者
     */
    private String createdBy;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
