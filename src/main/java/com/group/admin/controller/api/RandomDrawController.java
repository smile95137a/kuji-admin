package com.group.admin.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.group.admin.res.draw.DrawResponseRes;
import com.group.admin.res.draw.DrawResultRes;
import com.group.admin.service.DrawService;
import com.group.admin.service.SystemConfigService;
import com.group.admin.service.CoinService;
import com.group.admin.util.SecurityUtils;

import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 前台加權隨機抽獎 API
 * 
 * <p>路由：/api/lottery/random/**
 * <p>此 Controller 使用「加權隨機算法」進行抽獎，根據獎品權重決定中獎機率。
 * <p>與 /api/lottery/draw/** (籤位制) 不同，這是純隨機模式。
 * 
 * <h3>算法說明：</h3>
 * <ul>
 *   <li>總權重 = Σ(prize.weight)</li>
 *   <li>機率 = prize.weight / totalWeight</li>
 *   <li>例如：A賞(weight=5), B賞(weight=10), C賞(weight=20) → 總權重=35</li>
 *   <li>中獎機率：A賞=5/35(14%), B賞=10/35(29%), C賞=20/35(57%)</li>
 * </ul>
 * 
 * @author KUJI Team
 * @since 2025-12-25
 */
@RestController
@RequestMapping("/lottery/random")
@Validated
@Slf4j
public class RandomDrawController {
    
    @Autowired
    private DrawService drawService;
    
    @Autowired
    private CoinService walletService;

        @Autowired
        private SystemConfigService systemConfigService;
    
    /**
     * 執行加權隨機抽獎
     * 
     * <p><b>業務流程：</b>
     * <ol>
     *   <li>驗證一番賞狀態（ON_SHELF）</li>
     *   <li>查詢可抽獎品（remaining > 0）</li>
     *   <li>計算費用（pricePerDraw × count）</li>
     *   <li>驗證錢包餘額（gold + bonus >= cost）</li>
     *   <li>扣除點數（優先使用 Gold，不足時使用 Bonus 補足）</li>
     *   <li>執行加權隨機抽獎（根據 weight 權重）</li>
     *   <li>減少獎品庫存（remaining - 1）</li>
     *   <li>新增至賞品盒（PrizeBox）</li>
     *   <li>記錄錢包交易（WalletTransaction）</li>
     * </ol>
     * 
     * <p><b>支付邏輯：</b>
     * <ul>
     *   <li>if (gold >= cost) → 全部使用 Gold</li>
     *   <li>else → 先用完所有 Gold，剩餘用 Bonus 補足</li>
     * </ul>
     * 
     * <p><b>請求示例：</b>
     * <pre>
     * POST /api/lottery/random/{lotteryId}/draw?count=3
     * Authorization: Bearer {USER_JWT_TOKEN}
     * </pre>
     * 
     * <p><b>回應示例：</b>
     * <pre>
     * {
     *   "success": true,
     *   "data": {
     *     "results": [
     *       {
     *         "lotteryTitle": "鬼滅之刃一番賞",
     *         "prizeName": "炭治郎 手辦",
     *         "prizeLevel": "A",
     *         "prizeImageUrl": "https://...",
     *         "isGrandPrize": false,
     *         "isLastPrize": false,
     *         "costType": "GOLD",
     *         "costAmount": 80,
     *         "drawTime": "2025-12-25T10:30:00"
     *       },
     *       {
     *         "lotteryTitle": "鬼滅之刃一番賞",
     *         "prizeName": "禰豆子 吊飾",
     *         "prizeLevel": "C",
     *         "prizeImageUrl": "https://...",
     *         "isGrandPrize": false,
     *         "isLastPrize": false,
     *         "costType": "GOLD",
     *         "costAmount": 80,
     *         "drawTime": "2025-12-25T10:30:01"
     *       }
     *     ],
     *     "goldUsed": 160,
     *     "bonusUsed": 0,
     *     "remainingGold": 340,
     *     "remainingBonus": 0,
     *     "totalCount": 2
     *   }
     * }
     * </pre>
     * 
     * @param lotteryId 一番賞 ID
     * @param count 抽獎次數（1-10）
     * @return 抽獎結果（獎品列表 + 錢包餘額）
     */
    @PostMapping("/{lotteryId}/draw")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<DrawResponseRes> executeDraw(
            @PathVariable String lotteryId,
            @RequestParam 
            @Min(value = 1, message = "抽獎次數最少 1 次")
            Integer count) {

                int maxCount = systemConfigService.getInt(SystemConfigService.KEY_MAX_DRAWS_PER_REQUEST, 10);
                if (count > maxCount) {
                        return ResponseEntity.badRequest().build();
                }
        
        // 🎭 取得當前用戶 ID
        String userId = SecurityUtils.getCurrentUserId();
        log.info("🎲 用戶 {} 開始加權隨機抽獎，一番賞 ID: {}，次數: {}", userId, lotteryId, count);
        
        // 記錄抽獎前錢包餘額
        var walletBefore = walletService.getWallet(userId);
        Long goldBefore = walletBefore.getGoldCoins();
        Long bonusBefore = walletBefore.getBonusCoins();
        
        // 🎯 執行抽獎
        List<DrawResultRes> results = drawService.executeDraw(userId, lotteryId, count);
        
        // 查詢抽獎後錢包餘額
        var walletAfter = walletService.getWallet(userId);
        Long goldAfter = walletAfter.getGoldCoins();
        Long bonusAfter = walletAfter.getBonusCoins();
        
        // 計算實際使用的點數
        Long goldUsed = goldBefore - goldAfter;
        Long bonusUsed = bonusBefore - bonusAfter;
        
        // 建立回應
        DrawResponseRes response = DrawResponseRes.builder()
                .results(results)
                .goldUsed(goldUsed)
                .bonusUsed(bonusUsed)
                .remainingGold(goldAfter)
                .remainingBonus(bonusAfter)
                .totalCount(results.size())
                .build();
        
        log.info("✅ 抽獎完成，用戶 {}，獲得 {} 個獎品", userId, results.size());
        log.info("💰 點數使用：Gold: {}, Bonus: {}，剩餘：Gold: {}, Bonus: {}",
                goldUsed, bonusUsed, goldAfter, bonusAfter);
        
        return ResponseEntity.ok(response);
    }
}
