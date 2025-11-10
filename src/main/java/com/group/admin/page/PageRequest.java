package com.group.admin.page;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分頁請求參數
 * 用於接收前端的分頁查詢參數
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageRequest {
    
    @Min(0)
    private int page = 0;  // 第幾頁 (從 0 開始)
    
    @Min(1)
    @Max(999)
    private int size = 10;  // 每頁幾筆
    
    private String sortBy;  // 排序欄位
    
    private String sortDirection = "ASC";  // 排序方向: ASC 或 DESC
    
    /**
     * 計算要跳過的筆數
     */
    public int getOffset() {
        return page * size;
    }
    
    /**
     * 取得分頁大小
     */
    public int getLimit() {
        return size;
    }
}