package com.group.admin.page;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分頁回應資料
 * 包含列表資料和分頁資訊
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageData<T> {

    private List<T> items; // 資料列表

    private int page; // 當前頁碼

    private int pageSize; // 每頁筆數

    private long totalItems; // 總筆數

    private int totalPages; // 總頁數

    private boolean hasNext; // 是否有下一頁

    private boolean hasPrevious; // 是否有上一頁

    /**
     * 從 List 和總筆數建立分頁資料 (推薦用這個!)
     * 
     * @param items      當前頁的資料
     * @param page       當前頁碼
     * @param pageSize   每頁筆數
     * @param totalItems 總筆數
     */
    public static <T> PageData<T> of(List<T> items, int page, int pageSize, long totalItems) {
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        return PageData.<T>builder()
                .items(items)
                .page(page)
                .pageSize(pageSize)
                .totalItems(totalItems)
                .totalPages(totalPages)
                .hasNext(page < totalPages - 1)
                .hasPrevious(page > 0)
                .build();
    }

    /**
     * 從 List 建立 (不分頁的情況,全部資料)
     */
    public static <T> PageData<T> of(List<T> items) {
        return PageData.<T>builder()
                .items(items)
                .page(0)
                .pageSize(items.size())
                .totalItems(items.size())
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .build();
    }

    /**
     * 轉換資料類型 (例如 Entity 轉 DTO)
     */
    public <R> PageData<R> map(Function<T, R> mapper) {
        List<R> mappedItems = items.stream()
                .map(mapper)
                .collect(Collectors.toList());

        return PageData.<R>builder()
                .items(mappedItems)
                .page(page)
                .pageSize(pageSize)
                .totalItems(totalItems)
                .totalPages(totalPages)
                .hasNext(hasNext)
                .hasPrevious(hasPrevious)
                .build();
    }
}
