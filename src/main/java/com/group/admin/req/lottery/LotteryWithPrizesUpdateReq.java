package com.group.admin.req.lottery;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 商品與獎品整合更新請求 DTO
 * 
 * 一支 API 同時更新商品和獎品，前端不用分兩次呼叫。
 * 
 * <p><b>更新邏輯：</b>
 * <ul>
 *   <li>商品資訊：直接更新</li>
 *   <li>獎品列表：
 *     <ul>
 *       <li>有 ID 的獎品 → 更新</li>
 *       <li>沒有 ID 的獎品 → 新增</li>
 *       <li>資料庫有但前端沒傳的 → 保留（不刪除）</li>
 *     </ul>
 *   </li>
 * </ul>
 * 
 * <p><b>請求範例：</b>
 * <pre>
 * {
 *   "lottery": {
 *     "title": "鬼滅之刃一番賞（更新）",
 *     "pricePerDraw": 85,
 *     "status": "ON_SHELF"
 *   },
 *   "prizes": [
 *     {
 *       "id": "prize-uuid-1",  // 有 ID → 更新
 *       "name": "炭治郎 手辦（更新）",
 *       "quantity": 2
 *     },
 *     {
 *       // 沒有 ID → 新增
 *       "name": "伊之助 徽章",
 *       "level": "D",
 *       "quantity": 30,
 *       "weight": 25
 *     }
 *   ]
 * }
 * </pre>
 * 
 * @author KUJI Team
 * @since 2025-12-25
 */
@Data
@Schema(description = "商品與獎品整合更新請求（一支 API 完成商品+獎品）")
public class LotteryWithPrizesUpdateReq {
    
    /**
     * 商品 ID
     * 
     * ⚠️ 此欄位會由 Controller 從 URL 路徑參數自動設定，前端不需要在 Body 中傳送
     */
    @Schema(description = "商品 ID（由 URL 路徑參數自動設定）", example = "550e8400-e29b-41d4-a716-446655440000")
    private String lotteryId;
    
    /**
     * 商品更新資訊
     */
    @Valid
    @Schema(description = "商品更新資訊（選填欄位）")
    private LotteryUpdateReq lottery;
    
    /**
     * 獎品列表（可選）
     * 
     * ⚠️ 注意：
     * - 有 ID 的獎品會被更新
     * - 沒有 ID 的獎品會被新增
     * - 沒傳的獎品會保留（不刪除）
     */
    @Valid
    @Schema(description = "獎品列表（有 ID=更新，無 ID=新增，沒傳=保留）")
    private List<LotteryPrizeUpdateReq> prizes;
}
