package com.group.admin.service.impl;

import com.group.admin.entity.Lottery;
import com.group.admin.entity.LotteryPrize;
import com.group.admin.entity.User;
import com.group.admin.enums.LotteryStatusEnum;
import com.group.admin.enums.TransactionTypeEnum;
import com.group.admin.example.LotteryPrizeExample;
import com.group.admin.exception.BusinessException;
import com.group.admin.mapper.LotteryMapper;
import com.group.admin.mapper.LotteryPrizeMapper;
import com.group.admin.mapper.UserMapper;
import com.group.admin.req.draw.DrawReq;
import com.group.admin.res.draw.DrawResultRes;
import com.group.admin.service.DrawService;
import com.group.admin.service.PrizeBoxService;
import com.group.admin.service.CoinService;
import com.group.admin.service.ConsumptionRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 抽獎服務實作
 * 
 * 核心邏輯：
 * 1. 驗證商品狀態與庫存
 * 2. 計算消費金額並驗證錢包餘額
 * 3. 根據權重隨機抽取獎品
 * 4. 扣除點數（優先 Gold，不足時用 Bonus）
 * 5. 扣除獎品庫存
 * 6. 建立賞品盒記錄
 * 7. 記錄交易
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DrawServiceImpl implements DrawService {
    
    private final LotteryMapper lotteryMapper;
    private final LotteryPrizeMapper lotteryPrizeMapper;
    private final UserMapper userMapper;
    private final CoinService walletService;
    private final PrizeBoxService prizeBoxService;
    private final ConsumptionRecordService consumptionRecordService;
    private final Random random = new Random();
    
    @Override
    @Transactional
    public List<DrawResultRes> executeDraw(String userId, String lotteryId, Integer count) {
        log.info("🎰 開始抽獎：userId={}, lotteryId={}, count={}", userId, lotteryId, count);
        
        // ========== Step 1: 驗證商品 ==========
        Lottery lottery = lotteryMapper.selectByPrimaryKey(lotteryId);
        if (lottery == null) {
            throw new BusinessException("商品不存在");
        }
        
        if (!"ON_SHELF".equals(lottery.getStatus())) {
            throw new BusinessException("商品未上架，無法抽獎");
        }
        
        // ========== Step 2: 查詢獎品池 ==========
        LotteryPrizeExample prizeExample = new LotteryPrizeExample();
        prizeExample.createCriteria()
                .andLotteryIdEqualTo(lotteryId)
                .andRemainingGreaterThan(0);  // 只查詢有庫存的獎品
        
        List<LotteryPrize> availablePrizes = lotteryPrizeMapper.selectByExample(prizeExample);
        if (availablePrizes.isEmpty()) {
            throw new BusinessException("獎品已全部抽完");
        }
        
        log.info("📦 可用獎品數量：{}", availablePrizes.size());
        
        // ========== Step 3: 計算消費金額 ==========
        Long pricePerDraw = lottery.getPricePerDraw();
        if (pricePerDraw == null || pricePerDraw <= 0) {
            throw new BusinessException("商品價格設定錯誤");
        }
        
        Long totalCost = pricePerDraw * count;
        log.info("💰 總消費金額：{} (單抽: {}, 數量: {})", totalCost, pricePerDraw, count);
        
        // ========== Step 4: 驗證錢包餘額（依 paymentType）==========
        User user = userMapper.selectByPrimaryKey(userId);
        
        if (user == null) {
            throw new BusinessException("使用者不存在");
        }
        
        // 讀取 paymentType，預設為 GOLD（spec 019 尚未加此欄位時的安全預設值）
        String paymentType = "GOLD";
        
        Long gold = user.getGoldCoins() != null ? user.getGoldCoins() : 0L;
        Long bonus = user.getBonusCoins() != null ? user.getBonusCoins() : 0L;
        
        if ("BONUS".equals(paymentType)) {
            if (bonus < totalCost) {
                throw new BusinessException(String.format(
                    "紅利不足！需要 %d，但只有 %d", totalCost, bonus));
            }
        } else {
            if (gold < totalCost) {
                throw new BusinessException(String.format(
                    "金幣不足！需要 %d，但只有 %d", totalCost, gold));
            }
        }
        
        log.info("💳 錢包餘額：Gold={}, Bonus={}, paymentType={}", gold, bonus, paymentType);
        
        // ========== Step 5: 扣除點數 ==========
        Long goldUsed = 0L;
        Long bonusUsed = 0L;
        
        if ("BONUS".equals(paymentType)) {
            bonusUsed = totalCost;
            walletService.deductBonus(userId, bonusUsed,
                TransactionTypeEnum.DRAW.getCode(),
                lotteryId,
                String.format("抽獎消費：%s x%d", lottery.getTitle(), count));
        } else {
            goldUsed = totalCost;
            walletService.deductGold(userId, goldUsed,
                TransactionTypeEnum.DRAW.getCode(),
                lotteryId,
                String.format("抽獎消費：%s x%d", lottery.getTitle(), count));
        }
        
        log.info("💸 扣款成功：Gold={}, Bonus={}", goldUsed, bonusUsed);
        
        // ========== Step 6: 執行抽獎 ==========
        List<DrawResultRes> results = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            // Refresh available prizes each iteration (removes depleted prizes)
            prizeExample = new LotteryPrizeExample();
            prizeExample.createCriteria()
                    .andLotteryIdEqualTo(lotteryId)
                    .andRemainingGreaterThan(0);
            availablePrizes = lotteryPrizeMapper.selectByExample(prizeExample);
            
            if (availablePrizes.isEmpty()) {
                log.warn("⚠️ 第 {} 次抽獎時獎品已全部抽完", i + 1);
                break;
            }
            
            // --- Last-prize logic (US3) ---
            LotteryPrize drawnPrize = null;
            boolean isLastPrize = false;
            int totalRemaining = availablePrizes.stream()
                    .mapToInt(p -> p.getRemaining() != null ? p.getRemaining() : 0).sum();
            
            if (totalRemaining == 1) {
                // Check for designated last prize
                LotteryPrizeExample lastPrizeEx = new LotteryPrizeExample();
                lastPrizeEx.createCriteria()
                        .andLotteryIdEqualTo(lotteryId)
                        .andIsLastPrizeEqualTo((byte) 1)
                        .andRemainingGreaterThan(0);
                List<LotteryPrize> lastPrizes = lotteryPrizeMapper.selectByExample(lastPrizeEx);
                if (!lastPrizes.isEmpty()) {
                    drawnPrize = lastPrizes.get(0);
                    isLastPrize = true;
                    log.info("🏆 最後賞觸發: {}", drawnPrize.getName());
                }
            }
            
            if (drawnPrize == null) {
                drawnPrize = weightedRandomSelect(availablePrizes);
            }
            
            if (drawnPrize == null) {
                log.error("❌ 第 {} 次抽獎失敗：無可用獎品", i + 1);
                throw new BusinessException("抽獎失敗：無可用獎品");
            }
            
            // Decrement stock
            drawnPrize.setRemaining(drawnPrize.getRemaining() - 1);
            int updated = lotteryPrizeMapper.updateByPrimaryKeySelective(drawnPrize);
            if (updated == 0) {
                log.error("❌ 扣除獎品庫存失敗：prizeId={}", drawnPrize.getId());
                throw new BusinessException("獎品庫存不足");
            }
            
            // --- Auto-discount logic (US4) ---
            boolean priceChanged = false;
            Long newPrice = null;
            if (drawnPrize.getIsGrandPrize() != null && drawnPrize.getIsGrandPrize() == 1) {
                // Re-fetch lottery to get latest autoDiscountEnabled / discountedPrice
                lottery = lotteryMapper.selectByPrimaryKey(lotteryId);
                if (lottery != null
                        && lottery.getAutoDiscountEnabled() != null && lottery.getAutoDiscountEnabled() == 1
                        && lottery.getDiscountedPrice() != null && lottery.getDiscountedPrice() > 0) {
                    LotteryPrizeExample grandEx = new LotteryPrizeExample();
                    grandEx.createCriteria()
                            .andLotteryIdEqualTo(lotteryId)
                            .andIsGrandPrizeEqualTo((byte) 1)
                            .andRemainingGreaterThan(0);
                    long grandRemaining = lotteryPrizeMapper.countByExample(grandEx);
                    if (grandRemaining == 0) {
                        lotteryMapper.updatePriceAfterGrandPrizeSoldOut(lotteryId);
                        priceChanged = true;
                        newPrice = lottery.getDiscountedPrice();
                        log.info("💰 大獎售罄自動降價: lotteryId={}, newPrice={}", lotteryId, newPrice);
                    }
                }
            }
            
            // Add to prize box
            Long recycleBonus = calculateRecycleBonus(drawnPrize, lottery);
            prizeBoxService.addToPrizeBox(userId, lotteryId, drawnPrize.getId(), 
                lottery.getStoreId(), recycleBonus);
            
            // Build result
            DrawResultRes result = new DrawResultRes();
            result.setLotteryTitle(lottery.getTitle());
            result.setPrizeName(drawnPrize.getName());
            result.setPrizeLevel(drawnPrize.getLevel());
            result.setPrizeImageUrl(drawnPrize.getImageUrl());
            result.setIsGrandPrize(drawnPrize.getIsGrandPrize() != null && drawnPrize.getIsGrandPrize() == 1);
            result.setIsLastPrize(isLastPrize || (drawnPrize.getIsLastPrize() != null && drawnPrize.getIsLastPrize() == 1));
            result.setCostType("GOLD");
            result.setCostAmount(pricePerDraw);
            result.setDrawTime(java.time.LocalDateTime.now());
            if (priceChanged) {
                result.setDiscountTriggered(true);
                result.setDiscountedPrice(newPrice);
            }
            
            results.add(result);
            
            log.info("🎊 第 {} 次抽獎成功：{} ({}賞)", i + 1, drawnPrize.getName(), drawnPrize.getLevel());
        }
        
        log.info("✅ 抽獎完成：共 {} 次，消費 Gold={}, Bonus={}", count, goldUsed, bonusUsed);
        
        // ========== Step 7: 記錄消費記錄 ==========
        if (goldUsed > 0) {
            consumptionRecordService.recordConsumption(
                userId, 
                "DRAW_GOLD", 
                lotteryId, 
                lottery.getTitle(),
                null,  // orderId
                null,  // orderNumber
                goldUsed, 
                0L,
                String.format("使用金幣抽獎：%s x%d", lottery.getTitle(), count)
            );
        }
        
        if (bonusUsed > 0) {
            consumptionRecordService.recordConsumption(
                userId, 
                "DRAW_BONUS", 
                lotteryId, 
                lottery.getTitle(),
                null,  // orderId
                null,  // orderNumber
                0L,
                bonusUsed,
                String.format("使用紅利抽獎：%s x%d", lottery.getTitle(), count)
            );
        }
        
        return results;
    }
    
    /**
     * 根據權重隨機選擇獎品
     * 
     * 演算法：加權隨機
     * 1. 計算總權重
     * 2. 隨機產生 0 ~ 總權重的數字
     * 3. 累加權重，找到對應的獎品
     * 
     * @param prizes 可用獎品列表
     * @return 抽中的獎品
     */
    private LotteryPrize weightedRandomSelect(List<LotteryPrize> prizes) {
        if (prizes == null || prizes.isEmpty()) {
            return null;
        }
        
        // 計算總權重
        int totalWeight = prizes.stream()
                .mapToInt(p -> p.getWeight() != null ? p.getWeight() : 1)
                .sum();
        
        // 隨機產生 0 ~ totalWeight 的數字
        int randomValue = random.nextInt(totalWeight);
        
        // 累加權重，找到對應的獎品
        int cumulativeWeight = 0;
        for (LotteryPrize prize : prizes) {
            int weight = prize.getWeight() != null ? prize.getWeight() : 1;
            cumulativeWeight += weight;
            
            if (randomValue < cumulativeWeight) {
                log.debug("🎲 抽獎結果：隨機值={}, 總權重={}, 抽中={}", 
                    randomValue, totalWeight, prize.getName());
                return prize;
            }
        }
        
        // 理論上不會到達這裡，但作為保險返回最後一個
        return prizes.get(prizes.size() - 1);
    }
    
    /**
     * 計算獎品回收可得紅利
     * 
     * 規則：獎品價值的 50%
     * （可以依照獎項等級調整比例）
     * 
     * @param prize 獎品
     * @param lottery 商品
     * @return 回收紅利
     */
    private Long calculateRecycleBonus(LotteryPrize prize, Lottery lottery) {
        // 如果獎品有設定點數價值，使用該價值
        if (prize.getPointValue() != null && prize.getPointValue() > 0) {
            return prize.getPointValue() / 2;  // 50%
        }
        
        // 否則使用抽獎價格的 50%
        Long pricePerDraw = lottery.getPricePerDraw();
        if (pricePerDraw != null && pricePerDraw > 0) {
            return pricePerDraw / 2;
        }
        
        // 預設值
        return 10L;
    }
}
