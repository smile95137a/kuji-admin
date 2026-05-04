package com.group.admin.condition.report;

import com.group.admin.req.common.BaseCondition;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 平台營收總覽報表查詢條件
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "平台營收總覽報表查詢條件")
public class PlatformRevenueReportCondition extends BaseCondition {

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
