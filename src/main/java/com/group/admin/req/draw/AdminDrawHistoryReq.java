package com.group.admin.req.draw;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 後台抽獎歷史查詢請求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDrawHistoryReq {

    @Builder.Default
    private Integer page = 1;

    @Builder.Default
    private Integer size = 20;

    private String userId;

    private String status;

    private LocalDateTime startDate;

    private LocalDateTime endDate;
}
