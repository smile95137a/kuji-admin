package com.group.admin.condition.report;

import com.group.admin.req.common.BaseCondition;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 店家績效報表查詢條件
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "店家績效報表查詢條件")
public class StorePerformanceCondition extends BaseCondition {

    /**
     * 店家 ID（Admin 可留 null 查全部；StoreOwner 後端自動覆蓋為自己的 storeId）
     */
    @Schema(description = "店家 ID（Admin 可留 null 查全部；StoreOwner 後端自動覆蓋）", example = "uuid-string")
    private String storeId;

    /**
     * 開始日期
     */
    @Schema(description = "開始日期", example = "2026-04-01")
    private LocalDate startDate;

    /**
     * 結束日期
     */
    @Schema(description = "結束日期", example = "2026-04-30")
    private LocalDate endDate;
}
