package com.group.admin.condition.report;

import com.group.admin.req.common.BaseCondition;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品銷售排行報表查詢條件
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "商品銷售排行報表查詢條件")
public class LotterySalesRankingCondition extends BaseCondition {

    /**
     * 店家 ID（StoreOwner 後端自動帶入；Admin 可選填以過濾特定店家）
     */
    @Schema(description = "店家 ID（StoreOwner 後端強制帶入，Admin 可選填）", example = "uuid-string")
    private String storeId;

    /**
     * 回傳筆數，預設 20，最大 100
     */
    @Schema(description = "回傳筆數，預設 20，最大 100", example = "20")
    private Integer limit;
}
