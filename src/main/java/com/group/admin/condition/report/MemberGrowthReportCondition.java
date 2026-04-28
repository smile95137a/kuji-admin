package com.group.admin.condition.report;

import com.group.admin.req.common.BaseCondition;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 會員成長報表查詢條件
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "會員成長報表查詢條件")
public class MemberGrowthReportCondition extends BaseCondition {

    @Schema(description = "開始日期（預設：今天 - 29 天）", example = "2026-04-01")
    private LocalDate startDate;

    @Schema(description = "結束日期（預設：今天）", example = "2026-04-30")
    private LocalDate endDate;
}
