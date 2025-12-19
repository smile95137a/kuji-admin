package com.group.admin.page;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 統一分頁請求參數
 * 用於接收前端的分頁查詢參數
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageRequest {

    /**
     * 當前頁碼（從 1 開始，符合一般使用習慣）
     */
    @Min(value = 1, message = "頁碼必須大於等於 1")
    @Builder.Default
    private Integer page = 1;

    /**
     * 每頁大小（預設 20，最大 100）
     */
    @Min(value = 1, message = "每頁大小必須大於等於 1")
    @Max(value = 100, message = "每頁大小不可超過 100")
    @Builder.Default
    private Integer pageSize = 20;

    /**
     * 排序欄位（例如：createdAt, id）
     */
    private String sortBy;

    /**
     * 排序方向（ASC 或 DESC，預設 DESC）
     */
    @Builder.Default
    private String sortOrder = "DESC";

    /**
     * 計算 SQL OFFSET（跳過的筆數）
     * 例如：page=1 → offset=0, page=2 → offset=20
     */
    public int getOffset() {
        return (page - 1) * pageSize;
    }

    /**
     * 取得 SQL LIMIT（每頁筆數）
     */
    public int getLimit() {
        return pageSize;
    }

    /**
     * 驗證並修正分頁參數
     * 用於確保參數在合法範圍內
     */
    public void validate() {
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 20;
        }
        if (pageSize > 100) {
            pageSize = 100; // 限制最大每頁 100 筆，避免查詢過大
        }
        if (sortOrder == null || (!sortOrder.equalsIgnoreCase("ASC") && !sortOrder.equalsIgnoreCase("DESC"))) {
            sortOrder = "DESC";
        }
    }

    /**
     * 取得 MyBatis 可用的排序 SQL 片段
     * 注意：使用時需要驗證 sortBy 欄位名稱，避免 SQL Injection
     */
    public String getOrderByClause() {
        if (sortBy == null || sortBy.isBlank()) {
            return "";
        }
        return sortBy + " " + sortOrder;
    }
}