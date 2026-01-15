package com.group.admin.req.lottery;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品與獎品整合建立請求 DTO
 * 
 * 一支 API 同時建立商品和獎品，前端不用分兩次呼叫。
 * 
 * <p><b>使用場景：</b>
 * <ul>
 *   <li>新增商品時，同時批次建立所有獎品（A賞、B賞、C賞...）</li>
 *   <li>前端表單一次性提交商品基本資訊 + 獎品列表</li>
 *   <li>後端自動處理商品建立 → 獎品批次建立 → 返回完整資料</li>
 * </ul>
 * 
 * <p><b>請求範例：</b>
 * <pre>
 * {
 *   "lottery": {
 *     "title": "鬼滅之刃一番賞",
 *     "category": "OFFICIAL_ICHIBAN",
 *     "pricePerDraw": 80,
 *     "totalDraws": 100,
 *     "status": "OFF_SHELF"
 *   },
 *   "prizes": [
 *     {
 *       "name": "炭治郎 手辦",
 *       "level": "A",
 *       "quantity": 1,
 *       "weight": 5,
 *       "isGrandPrize": true
 *     },
 *     {
 *       "name": "禰豆子 吊飾",
 *       "level": "B",
 *       "quantity": 5,
 *       "weight": 10
 *     },
 *     {
 *       "name": "善逸 海報",
 *       "level": "C",
 *       "quantity": 20,
 *       "weight": 20
 *     }
 *   ]
 * }
 * </pre>
 * 
 * @author KUJI Team
 * @since 2025-12-25
 */
@Data
@Schema(description = "商品與獎品整合建立請求（一支 API 完成商品+獎品）")
public class LotteryWithPrizesCreateReq {
    
    /**
     * 商品基本資訊
     */
    @NotNull(message = "商品資訊不可為空")
    @Valid
    @Schema(description = "商品基本資訊", requiredMode = Schema.RequiredMode.REQUIRED)
    private LotteryCreateReq lottery;
    
    /**
     * 獎品列表（可選，但建議至少 1 個）
     */
    @Valid
    @Schema(description = "獎品列表（可選，建議新增時就設定）")
    private List<LotteryPrizeCreateReq> prizes;
}
