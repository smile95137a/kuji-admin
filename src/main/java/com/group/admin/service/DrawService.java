package com.group.admin.service;

import com.group.admin.res.draw.DrawResultRes;
import java.util.List;

/**
 * 抽獎服務介面（加權隨機模式）
 * 
 * 此服務用於「一般隨機抽」模式，根據獎品權重進行抽獎。
 * 與 LotteryTicketService（籤位制）不同，這是純隨機算法。
 * 
 * @author KUJI Team
 * @since 2025-12-25
 */
public interface DrawService {
    
    /**
     * 執行加權隨機抽獎
     * 
     * <p>業務流程：
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
     * @param userId 用戶 ID
     * @param lotteryId 一番賞 ID
     * @param count 抽獎次數（1-10）
     * @return 抽獎結果列表（每次抽獎的獎品詳情）
     */
    List<DrawResultRes> executeDraw(String userId, String lotteryId, Integer count);
}
