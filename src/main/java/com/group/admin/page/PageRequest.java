package com.group.admin.page;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 統一分頁請求參數
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageRequest {

    @Min(value = 1, message = "頁碼必須大於等於 1")
    private Integer page = 1;

    @Min(value = 1, message = "每頁大小必須大於等於 1")
    @Max(value = 100, message = "每頁大小不可超過 100")
    private Integer pageSize = 20;

    private String sortBy;

    private String sortOrder = "DESC";

    public int getOffset() {
        return (page - 1) * pageSize;
    }

    public int getLimit() {
        return pageSize;
    }

    public void validate() {
        if (page == null || page < 1) page = 1;
        if (pageSize == null || pageSize < 1) pageSize = 20;
        if (pageSize > 100) pageSize = 100;
        if (sortOrder == null || (!sortOrder.equalsIgnoreCase("ASC") && !sortOrder.equalsIgnoreCase("DESC"))) {
            sortOrder = "DESC";
        }
    }

    public String getOrderByClause() {
        if (sortBy == null || sortBy.isBlank()) return "";
        return sortBy + " " + sortOrder;
    }
}