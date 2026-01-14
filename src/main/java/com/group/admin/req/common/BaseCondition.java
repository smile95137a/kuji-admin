package com.group.admin.req.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

/**
 * 通用查詢條件基礎類別
 * 所有 Condition 類別應該繼承此類別
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Data
public abstract class BaseCondition {
    
    /**
     * 建立時間（起）
     * 支援格式：yyyy-MM-dd
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdAtStart;
    
    /**
     * 建立時間（迄）
     * 支援格式：yyyy-MM-dd
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdAtEnd;
    
    /**
     * 關鍵字搜尋（模糊查詢）
     * 子類別可以自行決定如何使用此欄位
     */
    private String keyword;
}
