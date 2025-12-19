package com.group.admin.res.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分頁回應封裝
 * 
 * <p>用於分頁查詢的標準回應格式</p>
 * 
 * @param <T> 資料類型
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分頁回應")
public class PageResponse<T> {

    /**
     * 資料列表
     */
    @Schema(description = "資料列表")
    private List<T> content;

    /**
     * 當前頁碼（從 0 開始）
     */
    @Schema(description = "當前頁碼（從 0 開始）", example = "0")
    private Integer pageNumber;

    /**
     * 每頁大小
     */
    @Schema(description = "每頁大小", example = "20")
    private Integer pageSize;

    /**
     * 總頁數
     */
    @Schema(description = "總頁數", example = "10")
    private Integer totalPages;

    /**
     * 總筆數
     */
    @Schema(description = "總筆數", example = "200")
    private Long totalElements;

    /**
     * 是否為第一頁
     */
    @Schema(description = "是否為第一頁")
    private Boolean first;

    /**
     * 是否為最後一頁
     */
    @Schema(description = "是否為最後一頁")
    private Boolean last;

    /**
     * 是否有下一頁
     */
    @Schema(description = "是否有下一頁")
    private Boolean hasNext;

    /**
     * 是否有上一頁
     */
    @Schema(description = "是否有上一頁")
    private Boolean hasPrevious;

    /**
     * 建構分頁回應
     * 
     * @param content 資料列表
     * @param pageNumber 當前頁碼
     * @param pageSize 每頁大小
     * @param totalElements 總筆數
     * @param <T> 資料類型
     * @return 分頁回應
     */
    public static <T> PageResponse<T> of(List<T> content, int pageNumber, int pageSize, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        
        return PageResponse.<T>builder()
                .content(content)
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .totalPages(totalPages)
                .totalElements(totalElements)
                .first(pageNumber == 0)
                .last(pageNumber >= totalPages - 1)
                .hasNext(pageNumber < totalPages - 1)
                .hasPrevious(pageNumber > 0)
                .build();
    }

    /**
     * 建構空的分頁回應
     * 
     * @param pageNumber 當前頁碼
     * @param pageSize 每頁大小
     * @param <T> 資料類型
     * @return 空的分頁回應
     */
    public static <T> PageResponse<T> empty(int pageNumber, int pageSize) {
        return PageResponse.<T>builder()
                .content(List.of())
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .totalPages(0)
                .totalElements(0L)
                .first(true)
                .last(true)
                .hasNext(false)
                .hasPrevious(false)
                .build();
    }
}
