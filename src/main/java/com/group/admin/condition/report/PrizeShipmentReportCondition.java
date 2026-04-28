package com.group.admin.condition.report;

import com.group.admin.req.common.BaseCondition;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 獎品出貨報表查詢條件
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "獎品出貨報表查詢條件")
public class PrizeShipmentReportCondition extends BaseCondition {

    /**
     * 店家 ID（後端自動帶入，StoreOwner 只能查自己店家）
     */
    @Schema(description = "店家 ID（後端自動帶入）")
    private String storeId;

    /**
     * 查詢開始日期（預設：今日 -29 天）
     */
    @Schema(description = "開始日期", example = "2026-04-01")
    private LocalDate startDate;

    /**
     * 查詢結束日期（預設：今日）
     */
    @Schema(description = "結束日期", example = "2026-04-30")
    private LocalDate endDate;
}
