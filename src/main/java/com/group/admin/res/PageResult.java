package com.group.admin.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分頁結果通用類
 *
 * @param <T> 資料類型
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分頁結果")
public class PageResult<T> {

    /**
     * 當前頁碼
     */
    @Schema(description = "當前頁碼", example = "1")
    private Integer page;

    /**
     * 每頁筆數
     */
    @Schema(description = "每頁筆數", example = "10")
    private Integer size;

    /**
     * 總筆數
     */
    @Schema(description = "總筆數", example = "100")
    private Long total;

    /**
     * 總頁數
     */
    @Schema(description = "總頁數", example = "10")
    private Integer totalPages;

    /**
     * 是否有下一頁
     */
    @Schema(description = "是否有下一頁", example = "true")
    private Boolean hasNext;

    /**
     * 是否有上一頁
     */
    @Schema(description = "是否有上一頁", example = "false")
    private Boolean hasPrevious;

    /**
     * 資料列表
     */
    @Schema(description = "資料列表")
    private List<T> data;

    /**
     * 建立分頁結果
     *
     * @param page  頁碼
     * @param size  每頁筆數
     * @param total 總筆數
     * @param data  資料列表
     */
    public static <T> PageResult<T> of(Integer page, Integer size, Long total, List<T> data) {
        PageResult<T> result = new PageResult<>();
        result.setPage(page);
        result.setSize(size);
        result.setTotal(total);
        result.setData(data);
        
        // 計算總頁數
        int totalPages = (int) Math.ceil((double) total / size);
        result.setTotalPages(totalPages);
        
        // 是否有下一頁
        result.setHasNext(page < totalPages);
        
        // 是否有上一頁
        result.setHasPrevious(page > 1);
        
        return result;
    }

    /**
     * 建立空分頁結果
     */
    public static <T> PageResult<T> empty(Integer page, Integer size) {
        return of(page, size, 0L, List.of());
    }
}
