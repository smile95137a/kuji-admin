package com.group.admin.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PaginationInfo {
    private int page;
    private int pageSize;
    private long totalItems;
    private int totalPages;
}
