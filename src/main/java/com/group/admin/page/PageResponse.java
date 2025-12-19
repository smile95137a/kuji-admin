package com.group.admin.page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 統一分頁回應格式
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    
    /**
     * 當前頁碼
     */
    private Integer page;
    
    /**
     * 每頁大小
     */
    private Integer pageSize;
    
    /**
     * 總筆數
     */
    private Long total;
    
    /**
     * 總頁數
     */
    private Integer totalPages;
    
    /**
     * 當前頁資料
     */
    private List<T> items;
    
    /**
     * 是否有下一頁
     */
    private Boolean hasNext;
    
    /**
     * 是否有上一頁
     */
    private Boolean hasPrevious;
    
    /**
     * 建立分頁回應的靜態工廠方法
     */
    public static <T> PageResponse<T> of(List<T> items, Long total, PageRequest pageRequest) {
        int totalPages = (int) Math.ceil((double) total / pageRequest.getPageSize());
        
        return PageResponse.<T>builder()
                .page(pageRequest.getPage())
                .pageSize(pageRequest.getPageSize())
                .total(total)
                .totalPages(totalPages)
                .items(items)
                .hasNext(pageRequest.getPage() < totalPages)
                .hasPrevious(pageRequest.getPage() > 1)
                .build();
    }
    
    /**
     * 建立空分頁回應
     */
    public static <T> PageResponse<T> empty(PageRequest pageRequest) {
        return PageResponse.<T>builder()
                .page(pageRequest.getPage())
                .pageSize(pageRequest.getPageSize())
                .total(0L)
                .totalPages(0)
                .items(List.of())
                .hasNext(false)
                .hasPrevious(false)
                .build();
    }
}
