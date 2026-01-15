package com.group.admin.controller.admin;

import com.group.admin.req.lottery.LotteryWithPrizesCreateReq;
import com.group.admin.req.lottery.LotteryWithPrizesUpdateReq;
import com.group.admin.res.lottery.LotteryWithPrizesRes;
import com.group.admin.service.LotteryService;
import com.group.admin.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 後台商品與獎品整合管理 API
 * 
 * <p>路由：/admin/lottery-with-prizes/**
 * <p>角色：ROLE_ADMIN, ROLE_STORE_OWNER, ROLE_STORE_EDITOR
 * 
 * <h3>核心特色：</h3>
 * <ul>
 *   <li>✅ <b>一支 API 完成商品 + 獎品</b>：前端不用分兩次呼叫</li>
 *   <li>✅ <b>新增商品時同時建立獎品</b>：POST /admin/lottery-with-prizes</li>
 *   <li>✅ <b>更新商品時同時更新獎品</b>：PUT /admin/lottery-with-prizes/{id}</li>
 *   <li>✅ <b>查詢商品時包含獎品列表</b>：GET /admin/lottery-with-prizes/{id}</li>
 *   <li>✅ <b>支援部分更新</b>：只傳需要更新的欄位</li>
 * </ul>
 * 
 * <h3>與原 API 的差異：</h3>
 * <table border="1">
 *   <tr>
 *     <th>原 API</th>
 *     <th>整合 API</th>
 *   </tr>
 *   <tr>
 *     <td>POST /admin/lottery<br>POST /admin/lotteries/{id}/prizes</td>
 *     <td>POST /admin/lottery-with-prizes（一次完成）</td>
 *   </tr>
 *   <tr>
 *     <td>PUT /admin/lottery/{id}<br>PUT /admin/lotteries/prizes/{prizeId}</td>
 *     <td>PUT /admin/lottery-with-prizes/{id}（一次完成）</td>
 *   </tr>
 *   <tr>
 *     <td>GET /admin/lottery/{id}<br>GET /admin/lotteries/{id}/prizes</td>
 *     <td>GET /admin/lottery-with-prizes/{id}（一次返回）</td>
 *   </tr>
 * </table>
 * 
 * <h3>請求範例（新增）：</h3>
 * <pre>
 * POST /admin/lottery-with-prizes
 * {
 *   "lottery": {
 *     "title": "鬼滅之刃一番賞",
 *     "category": "OFFICIAL_ICHIBAN",
 *     "pricePerDraw": 80,
 *     "totalDraws": 100
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
 *     }
 *   ]
 * }
 * </pre>
 * 
 * @author KUJI Team
 * @since 2025-12-25
 */
@RestController
@RequestMapping("/admin/lottery-with-prizes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "後台商品與獎品整合管理", description = "一支 API 完成商品+獎品的 CRUD")
public class AdminLotteryWithPrizesController {
    
    private final LotteryService lotteryService;
    
    /**
     * 建立商品並同時新增獎品
     * 
     * <p><b>業務流程：</b>
     * <ol>
     *   <li>建立商品（Lottery）→ 返回 lotteryId</li>
     *   <li>批次新增獎品（LotteryPrize[]）→ 每個獎品關聯 lotteryId</li>
     *   <li>查詢完整資料（包含獎品列表）→ 返回前端</li>
     * </ol>
     * 
     * <p><b>自動處理：</b>
     * <ul>
     *   <li>StoreOwner/Editor：自動帶入第一個店家 ID</li>
     *   <li>Admin：必須明確指定 storeId</li>
     *   <li>獎品的 lotteryId 自動設定</li>
     *   <li>獎品的 remaining 自動設為 quantity</li>
     * </ul>
     * 
     * @param req 商品與獎品整合建立請求
     * @return 完整的商品與獎品資訊
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    @Operation(summary = "建立商品與獎品", description = "一支 API 同時建立商品和獎品")
    public ResponseEntity<LotteryWithPrizesRes> createLotteryWithPrizes(
            @Valid @RequestBody LotteryWithPrizesCreateReq req) {
        
        String userId = SecurityUtils.getCurrentUserId();
        log.info("📦 建立商品與獎品: userId={}, title={}, prizeCount={}", 
                userId, 
                req.getLottery().getTitle(),
                req.getPrizes() != null ? req.getPrizes().size() : 0);
        
        LotteryWithPrizesRes result = lotteryService.createLotteryWithPrizes(req, userId);
        
        log.info("✅ 建立成功: lotteryId={}, title={}, prizeCount={}", 
                result.getId(), result.getTitle(), result.getTotalPrizeCount());
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 更新商品並同時更新獎品
     * 
     * <p><b>更新邏輯：</b>
     * <ul>
     *   <li>商品資訊：直接更新（只更新非 null 欄位）</li>
     *   <li>獎品列表：
     *     <ul>
     *       <li>有 ID 的獎品 → 更新</li>
     *       <li>沒有 ID 的獎品 → 新增</li>
     *       <li>資料庫有但前端沒傳的 → 保留（不刪除）</li>
     *     </ul>
     *   </li>
     * </ul>
     * 
     * <p><b>使用場景：</b>
     * <ul>
     *   <li>只更新商品資訊：傳 lottery，不傳 prizes</li>
     *   <li>只更新獎品：傳 prizes，不傳 lottery</li>
     *   <li>同時更新：兩者都傳</li>
     * </ul>
     * 
     * <p><b>請求範例（同時更新）：</b>
     * <pre>
     * PUT /admin/lottery-with-prizes/{lotteryId}
     * {
     *   "lottery": {
     *     "title": "鬼滅之刃一番賞（更新）",
     *     "pricePerDraw": 85
     *   },
     *   "prizes": [
     *     {
     *       "id": "prize-uuid-1",  // 有 ID → 更新
     *       "name": "炭治郎 手辦（更新）"
     *     },
     *     {
     *       // 沒有 ID → 新增
     *       "name": "伊之助 徽章",
     *       "level": "D",
     *       "quantity": 30
     *     }
     *   ]
     * }
     * </pre>
     * 
     * @param lotteryId 商品 ID
     * @param req 商品與獎品整合更新請求
     * @return 完整的商品與獎品資訊
     */
    @PutMapping("/{lotteryId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    @Operation(summary = "更新商品與獎品", description = "一支 API 同時更新商品和獎品（支援部分更新）")
    public ResponseEntity<LotteryWithPrizesRes> updateLotteryWithPrizes(
            @PathVariable String lotteryId,
            @Valid @RequestBody LotteryWithPrizesUpdateReq req) {
        
        String userId = SecurityUtils.getCurrentUserId();
        log.info("📝 更新商品與獎品: userId={}, lotteryId={}, prizeCount={}", 
                userId, lotteryId,
                req.getPrizes() != null ? req.getPrizes().size() : 0);
        
        // 設定 lotteryId
        req.setLotteryId(lotteryId);
        
        LotteryWithPrizesRes result = lotteryService.updateLotteryWithPrizes(req, userId);
        
        log.info("✅ 更新成功: lotteryId={}, title={}, prizeCount={}", 
                result.getId(), result.getTitle(), result.getTotalPrizeCount());
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 查詢商品詳情（包含獎品列表）
     * 
     * <p><b>回應內容：</b>
     * <ul>
     *   <li>商品完整資訊（title, description, pricePerDraw, status...）</li>
     *   <li>所有獎品列表（name, level, quantity, remaining, weight...）</li>
     *   <li>統計資訊：
     *     <ul>
     *       <li>totalPrizeCount：獎品總數量</li>
     *       <li>remainingPrizeCount：剩餘獎品數量</li>
     *       <li>progressPercentage：抽獎進度百分比</li>
     *     </ul>
     *   </li>
     * </ul>
     * 
     * @param lotteryId 商品 ID
     * @return 完整的商品與獎品資訊
     */
    @GetMapping("/{lotteryId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    @Operation(summary = "查詢商品與獎品", description = "一支 API 返回商品和獎品完整資訊")
    public ResponseEntity<LotteryWithPrizesRes> getLotteryWithPrizes(
            @PathVariable String lotteryId) {
        
        String userId = SecurityUtils.getCurrentUserId();
        log.info("🔍 查詢商品與獎品: userId={}, lotteryId={}", userId, lotteryId);
        
        LotteryWithPrizesRes result = lotteryService.getLotteryWithPrizes(lotteryId);
        
        log.info("✅ 查詢成功: lotteryId={}, title={}, prizeCount={}", 
                result.getId(), result.getTitle(), result.getTotalPrizeCount());
        
        return ResponseEntity.ok(result);
    }
}
