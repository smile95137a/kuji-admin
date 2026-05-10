package com.group.admin.condition.report;

import com.group.admin.req.common.BaseCondition;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 推薦碼報表查詢條件
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "推薦碼報表查詢條件")
public class ReferralReportCondition extends BaseCondition {
    
    /**
     * 推薦店家 ID（Admin 篩選用）
     */
    @Schema(description = "推薦店家 ID（Admin 篩選用）", example = "uuid-string")
    private String storeId;
    
    /**
     * 開始日期
     */
    @Schema(description = "開始日期", example = "2026-01-01", required = true)
    private LocalDate startDate;
    
    /**
     * 結束日期
     */
    @Schema(description = "結束日期", example = "2026-02-11", required = true)
    private LocalDate endDate;
}
