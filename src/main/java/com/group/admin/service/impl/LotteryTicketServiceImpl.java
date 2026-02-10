package com.group.admin.service.impl;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.group.admin.entity.Lottery;
import com.group.admin.entity.LotteryPrize;
import com.group.admin.entity.LotterySession;
import com.group.admin.entity.LotteryTicket;
import com.group.admin.example.LotteryPrizeExample;
import com.group.admin.example.LotterySessionExample;
import com.group.admin.example.LotteryTicketExample;
import com.group.admin.exception.BusinessException;
import com.group.admin.mapper.LotteryMapper;
import com.group.admin.mapper.LotteryPrizeMapper;
import com.group.admin.mapper.LotterySessionMapper;
import com.group.admin.mapper.LotteryTicketMapper;
import com.group.admin.res.lottery.LotteryTicketRes;
import com.group.admin.service.ConsumptionRecordService;
import com.group.admin.service.LotteryTicketService;
import com.group.admin.service.PrizeBoxService;
import com.group.admin.service.WalletService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 籤位服務實作
 * 
 * <p>核心設計原則：</p>
 * <ul>
 *   <li>共用底層抽獎邏輯（都是抽一個號碼）</li>
 *   <li>不同模式差異在籤位生成方式</li>
 *   <li>前台 API 絕不洩漏未抽籤位的獎品資訊</li>
 * </ul>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LotteryTicketServiceImpl implements LotteryTicketService {

    private final LotteryMapper lotteryMapper;
    private final LotteryPrizeMapper lotteryPrizeMapper;
    private final LotteryTicketMapper lotteryTicketMapper;
    private final LotterySessionMapper lotterySessionMapper;
    private final PrizeBoxService prizeBoxService;
    private final WalletService walletService;
    private final ConsumptionRecordService consumptionRecordService;

    private final SecureRandom random = new SecureRandom();

    // ==================== 籤位生成 ====================

    @Override
    @Transactional
    public void generateTickets(String lotteryId) {
        log.info("🎰 開始生成籤位: lotteryId={}", lotteryId);
        
        Lottery lottery = lotteryMapper.selectByPrimaryKey(lotteryId);
        if (lottery == null) {
            throw new BusinessException("抽獎活動不存在: " + lotteryId);
        }

        // 🆕 使用 playMode 欄位（新架構）
        String gameMode = lottery.getPlayMode();
        int totalTickets = lottery.getMaxDraws();
        
        if (totalTickets <= 0) {
            throw new BusinessException("總抽數必須大於 0");
        }

        log.info("🎮 遊戲模式: {}, 總籤位數: {}", gameMode, totalTickets);

        // 根據遊戲模式生成籤位
        switch (gameMode != null ? gameMode : "LOTTERY_MODE") {
            case "LOTTERY_MODE" -> generateRandomTickets(lotteryId, totalTickets);
            case "SCRATCH_MODE", "SCRATCH_CARD_MODE" -> generateScratchTickets(lotteryId, totalTickets, lottery);
            default -> generateRandomTickets(lotteryId, totalTickets);
        }

        log.info("✅ 籤位生成完成: lotteryId={}, totalTickets={}", lotteryId, totalTickets);
    }

    /**
     * 生成隨機分配籤位（一番賞/扭蛋/卡牌）
     * 
     * <p>演算法：</p>
     * <ol>
     *   <li>取得所有獎品及其數量</li>
     *   <li>建立獎品池（每個獎品根據數量重複放入）</li>
     *   <li>打亂獎品池順序</li>
     *   <li>依序分配到籤位 1-N</li>
     * </ol>
     */
    private void generateRandomTickets(String lotteryId, int totalTickets) {
        log.info("🎲 生成隨機籤位: lotteryId={}, total={}", lotteryId, totalTickets);

        // 取得所有獎品
        LotteryPrizeExample prizeExample = new LotteryPrizeExample();
        prizeExample.createCriteria().andLotteryIdEqualTo(lotteryId);
        prizeExample.setOrderByClause("order_num ASC");
        List<LotteryPrize> prizes = lotteryPrizeMapper.selectByExample(prizeExample);
        
        if (prizes.isEmpty()) {
            log.warn("⚠️ 商品 {} 沒有設定獎品，無法生成籤位", lotteryId);
            return;
        }
        
        // 建立獎品池（每個獎品根據數量重複放入）
        List<PrizeSlot> prizePool = new ArrayList<>();
        for (LotteryPrize prize : prizes) {
            int quantity = prize.getQuantity() != null ? prize.getQuantity() : 0;
            for (int i = 0; i < quantity; i++) {
                prizePool.add(new PrizeSlot(prize.getId(), prize.getLevel()));
            }
        }
        
        log.info("📦 獎品池大小: {}, 總籤位數: {}", prizePool.size(), totalTickets);
        
        // ⚠️ 一番賞/扭蛋/卡牌模式：獎品總數必須 = 總籤位數（不能有謝謝惠顧）
        if (prizePool.size() != totalTickets) {
            throw new BusinessException(
                String.format("一番賞/扭蛋/卡牌模式：獎品總數(%d)必須等於總籤位數(%d)！每個籤位都應該有獎品，不能有謝謝惠顧。",
                    prizePool.size(), totalTickets)
            );
        }
        
        // 打亂順序（核心：隨機分配）
        Collections.shuffle(prizePool, random);
        
        // 批量生成籤位資料
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < totalTickets; i++) {
            LotteryTicket ticket = new LotteryTicket();
            ticket.setId(UUID.randomUUID().toString());
            ticket.setLotteryId(lotteryId);
            ticket.setTicketNumber(i + 1);
            ticket.setPrizeId(prizePool.get(i).prizeId());
            ticket.setPrizeLevel(prizePool.get(i).level());
            ticket.setStatus("AVAILABLE");
            ticket.setIsDesignatedPrize((byte) 0);
            ticket.setCreatedAt(now);
            ticket.setUpdatedAt(now);
            lotteryTicketMapper.insert(ticket);
        }
        
        log.info("✅ 隨機籤位生成完成，獎品分配範例: 1號={}, 2號={}, ...{}號={}", 
                prizePool.get(0).level(), 
                prizePool.get(1).level(),
                totalTickets,
                prizePool.get(totalTickets - 1).level());
    }

    /**
     * 生成刮刮樂籤位
     * 
     * <p>規則：</p>
     * <ul>
     *   <li>獎品會按照獎品數量分配到隨機位置</li>
     *   <li>剩餘的籤位自動設為「謝謝惠顧」</li>
     *   <li>店家指定模式：可以在建立時指定大獎位置</li>
     *   <li>玩家指定模式：等開套玩家指定大獎位置</li>
     * </ul>
     */
    private void generateScratchTickets(String lotteryId, int totalTickets, Lottery lottery) {
        log.info("🎫 生成刮刮樂籤位: lotteryId={}, total={}", lotteryId, totalTickets);

        // 取得所有獎品
        LotteryPrizeExample prizeExample = new LotteryPrizeExample();
        prizeExample.createCriteria().andLotteryIdEqualTo(lotteryId);
        prizeExample.setOrderByClause("order_num ASC");
        List<LotteryPrize> prizes = lotteryPrizeMapper.selectByExample(prizeExample);
        
        // 建立獎品池
        List<PrizeSlot> prizePool = new ArrayList<>();
        int totalPrizeCount = 0;
        
        for (LotteryPrize prize : prizes) {
            int quantity = prize.getQuantity() != null ? prize.getQuantity() : 0;
            totalPrizeCount += quantity;
            for (int i = 0; i < quantity; i++) {
                prizePool.add(new PrizeSlot(prize.getId(), prize.getLevel()));
            }
        }
        
        log.info("📦 刮刮樂獎品池: 獎品數={}, 謝謝惠顧數={}, 總籤位={}", 
                totalPrizeCount, totalTickets - totalPrizeCount, totalTickets);
        
        // 補齊「謝謝惠顧」
        int thanksCount = totalTickets - totalPrizeCount;
        for (int i = 0; i < thanksCount; i++) {
            prizePool.add(new PrizeSlot(null, "THANKS"));
        }
        
        // 打亂順序（隨機分配）
        Collections.shuffle(prizePool, random);
        
        // 批量生成籤位
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < totalTickets; i++) {
            LotteryTicket ticket = new LotteryTicket();
            ticket.setId(UUID.randomUUID().toString());
            ticket.setLotteryId(lotteryId);
            ticket.setTicketNumber(i + 1);
            ticket.setPrizeId(prizePool.get(i).prizeId());
            ticket.setPrizeLevel(prizePool.get(i).level());
            ticket.setStatus("AVAILABLE");
            ticket.setIsDesignatedPrize((byte) 0);
            ticket.setCreatedAt(now);
            ticket.setUpdatedAt(now);
            lotteryTicketMapper.insert(ticket);
        }
        
        log.info("✅ 刮刮樂籤位生成完成，獎品 {} 個，謝謝惠顧 {} 個", 
                totalPrizeCount, thanksCount);
    }

    @Override
    @Transactional
    public void designatePrizePositions(String lotteryId, String userId, List<Integer> prizeNumbers) {
        log.info("🎯 開套玩家指定大獎位置: lotteryId={}, userId={}, numbers={}", 
                lotteryId, userId, prizeNumbers);
        
        if (prizeNumbers == null || prizeNumbers.isEmpty()) {
            throw new BusinessException("請指定大獎位置");
        }
        
        // 取得大獎獎品
        LotteryPrizeExample prizeExample = new LotteryPrizeExample();
        prizeExample.createCriteria()
                .andLotteryIdEqualTo(lotteryId)
                .andIsGrandPrizeEqualTo((byte) 1);
        List<LotteryPrize> grandPrizes = lotteryPrizeMapper.selectByExample(prizeExample);
        
        if (grandPrizes.isEmpty()) {
            throw new BusinessException("商品未設定大獎獎品");
        }
        
        LotteryPrize grandPrize = grandPrizes.get(0);
        
        // 更新指定籤位為大獎
        for (Integer ticketNumber : prizeNumbers) {
            LotteryTicketExample ticketExample = new LotteryTicketExample();
            ticketExample.createCriteria()
                    .andLotteryIdEqualTo(lotteryId)
                    .andTicketNumberEqualTo(ticketNumber)
                    .andStatusEqualTo("AVAILABLE");
            
            List<LotteryTicket> tickets = lotteryTicketMapper.selectByExample(ticketExample);
            if (!tickets.isEmpty()) {
                LotteryTicket ticket = tickets.get(0);
                ticket.setPrizeId(grandPrize.getId());
                ticket.setPrizeLevel(grandPrize.getLevel());
                ticket.setIsDesignatedPrize((byte) 1);
                ticket.setDesignatedBy(userId);
                ticket.setUpdatedAt(LocalDateTime.now());
                lotteryTicketMapper.updateByPrimaryKey(ticket);
            }
        }
        
        log.info("✅ 大獎位置指定完成: {}", prizeNumbers);
    }

    // ==================== 前台籤位查詢 ====================

    @Override
    public List<LotteryTicketRes> getTicketsForFrontend(String lotteryId) {
        log.info("🔍 前台查詢籤位: lotteryId={}", lotteryId);
        
        LotteryTicketExample example = new LotteryTicketExample();
        example.createCriteria().andLotteryIdEqualTo(lotteryId);
        example.setOrderByClause("ticket_number ASC");
        List<LotteryTicket> tickets = lotteryTicketMapper.selectByExample(example);
        
        // 轉換並過濾敏感資訊（未抽籤位隱藏獎品資訊）
        List<LotteryTicketRes> result = new ArrayList<>();
        for (LotteryTicket ticket : tickets) {
            LotteryTicketRes res = toRes(ticket);
            // ⚠️ 關鍵：隱藏未抽籤位的獎品資訊（避免玩家通過圖片猜到大獎位置）
            if ("AVAILABLE".equals(ticket.getStatus())) {
                res.setPrizeId(null);
                res.setPrizeName(null);
                res.setPrizeLevel(null);
                res.setPrizeImageUrl(null);           // ← 隱藏圖片 URL
                res.setIsGrandPrize(null);            // ← 隱藏是否為大獎
                res.setIsLastPrize(null);             // ← 隱藏是否為最後賞
            }
            result.add(res);
        }
        
        return result;
    }

    @Override
    public List<LotteryTicketRes> getTicketsForBackend(String lotteryId) {
        log.info("🔍 後台查詢籤位: lotteryId={}", lotteryId);
        
        LotteryTicketExample example = new LotteryTicketExample();
        example.createCriteria().andLotteryIdEqualTo(lotteryId);
        example.setOrderByClause("ticket_number ASC");
        List<LotteryTicket> tickets = lotteryTicketMapper.selectByExample(example);
        
        // 後台返回完整資訊
        List<LotteryTicketRes> result = new ArrayList<>();
        for (LotteryTicket ticket : tickets) {
            result.add(toRes(ticket));
        }
        
        return result;
    }
    
    /**
     * 將 Entity 轉換為 Res
     */
    private LotteryTicketRes toRes(LotteryTicket ticket) {
        LotteryTicketRes res = new LotteryTicketRes();
        res.setId(ticket.getId());
        res.setTicketNumber(ticket.getTicketNumber());
        res.setPrizeId(ticket.getPrizeId());
        res.setPrizeLevel(ticket.getPrizeLevel());
        res.setStatus(ticket.getStatus());
        res.setDrawnAt(ticket.getDrawnAt());
        res.setIsDesignatedPrize(ticket.getIsDesignatedPrize() != null && ticket.getIsDesignatedPrize() == 1);
        
        // 如果有 prizeId，查詢獎品名稱
        if (ticket.getPrizeId() != null) {
            LotteryPrize prize = lotteryPrizeMapper.selectByPrimaryKey(ticket.getPrizeId());
            if (prize != null) {
                res.setPrizeName(prize.getName());
                res.setPrizeImageUrl(prize.getImageUrl());
                res.setIsGrandPrize(prize.getIsGrandPrize() != null && prize.getIsGrandPrize() == 1);
                res.setIsLastPrize(prize.getIsLastPrize() != null && prize.getIsLastPrize() == 1);
            }
        }
        
        return res;
    }

    // ==================== 抽獎核心邏輯 ====================

    @Override
    @Transactional
    public DrawResult draw(String lotteryId, String userId, Integer ticketNumber, int drawCount) {
        log.info("🎰 執行抽獎: lotteryId={}, userId={}, ticketNumber={}, count={}", 
                lotteryId, userId, ticketNumber, drawCount);
        
        // 1. 檢查商品狀態
        Lottery lottery = lotteryMapper.selectByPrimaryKey(lotteryId);
        if (lottery == null) {
            return new DrawResult(false, null, 0, null, null, null, null, false, false, 0L, "商品不存在");
        }
        if (!"ON_SHELF".equals(lottery.getStatus())) {
            return new DrawResult(false, null, 0, null, null, null, null, false, false, 0L, "商品未上架");
        }
        
        // 2. 檢查保護時間
        if (!canDrawNow(lotteryId, userId)) {
            return new DrawResult(false, null, 0, null, null, null, null, false, false, 0L, 
                    "商品正在被其他玩家抽獎中，請稍後再試");
        }
        
        // 3. 決定籤位
        int actualTicketNumber;
        LotteryTicket targetTicket;
        
        if (ticketNumber != null) {
            // 選號模式：檢查該籤位是否可用
            LotteryTicketExample example = new LotteryTicketExample();
            example.createCriteria()
                    .andLotteryIdEqualTo(lotteryId)
                    .andTicketNumberEqualTo(ticketNumber)
                    .andStatusEqualTo("AVAILABLE");
            List<LotteryTicket> tickets = lotteryTicketMapper.selectByExample(example);
            
            if (tickets.isEmpty()) {
                return new DrawResult(false, null, 0, null, null, null, null, false, false, 0L, 
                        "該籤位已被抽走或不存在");
            }
            targetTicket = tickets.get(0);
            actualTicketNumber = ticketNumber;
        } else {
            // 隨機模式
            Integer randomNumber = getRandomAvailableTicket(lotteryId);
            if (randomNumber == null) {
                return new DrawResult(false, null, 0, null, null, null, null, false, false, 0L, "已無可抽籤位");
            }
            
            // 取得該籤位
            LotteryTicketExample example = new LotteryTicketExample();
            example.createCriteria()
                    .andLotteryIdEqualTo(lotteryId)
                    .andTicketNumberEqualTo(randomNumber);
            List<LotteryTicket> tickets = lotteryTicketMapper.selectByExample(example);
            
            if (tickets.isEmpty()) {
                return new DrawResult(false, null, 0, null, null, null, null, false, false, 0L, "籤位查詢失敗");
            }
            targetTicket = tickets.get(0);
            actualTicketNumber = randomNumber;
        }
        
        // 4. 更新籤位狀態為已抽
        targetTicket.setStatus("DRAWN");
        targetTicket.setDrawnBy(userId);
        targetTicket.setDrawnAt(LocalDateTime.now());
        targetTicket.setUpdatedAt(LocalDateTime.now());
        lotteryTicketMapper.updateByPrimaryKey(targetTicket);
        
        log.info("✅ 籤位更新成功: ticketNumber={}, prizeLevel={}", 
                actualTicketNumber, targetTicket.getPrizeLevel());
        
        // 5. 取得獎品資訊
        String prizeId = targetTicket.getPrizeId();
        String prizeLevel = targetTicket.getPrizeLevel();
        String prizeName = null;
        String prizeImageUrl = null;
        boolean isGrandPrize = false;
        
        if (prizeId != null) {
            LotteryPrize prize = lotteryPrizeMapper.selectByPrimaryKey(prizeId);
            if (prize != null) {
                prizeName = prize.getName();
                prizeImageUrl = prize.getImageUrl();
                isGrandPrize = prize.getIsGrandPrize() != null && prize.getIsGrandPrize() == 1;
                
                // 減少獎品剩餘數量
                if (prize.getRemaining() != null && prize.getRemaining() > 0) {
                    prize.setRemaining(prize.getRemaining() - 1);
                    prize.setUpdatedAt(LocalDateTime.now());
                    lotteryPrizeMapper.updateByPrimaryKey(prize);
                }
            }
        } else {
            prizeName = "謝謝惠顧";
        }
        
        // 6. 更新商品的 totalDraws
        if (lottery.getTotalDraws() == null) {
            lottery.setTotalDraws(1);
        } else {
            lottery.setTotalDraws(lottery.getTotalDraws() + 1);
        }
        lottery.setUpdatedAt(LocalDateTime.now());
        lotteryMapper.updateByPrimaryKey(lottery);
        
        // 7. 將獎品加入賞品盒（如果不是謝謝惠顧）
        if (prizeId != null) {
            try {
                // 計算回收紅利（例如：原價的 50%）
                Long recycleBonus = lottery.getPricePerDraw() != null 
                        ? lottery.getPricePerDraw() / 2 
                        : 0L;
                
                prizeBoxService.addToPrizeBox(
                        userId, 
                        lotteryId, 
                        prizeId, 
                        lottery.getStoreId(), 
                        recycleBonus
                );
                log.info("✅ 獎品已加入賞品盒: userId={}, prizeId={}", userId, prizeId);
            } catch (Exception e) {
                log.error("⚠️ 獎品加入賞品盒失敗: {}", e.getMessage());
                // 不影響抽獎結果，只記錄錯誤
            }
        }
        
        // 8. 扣款、記錄抽獎紀錄、檢查免單
        Long pricePerDraw = lottery.getPricePerDraw() != null ? lottery.getPricePerDraw() : 0L;
        boolean triggeredFreeDraw = false;
        Long refundAmount = 0L;
        
        // 取得或建立 Session
        SessionInfo sessionInfo = getOrCreateSession(lotteryId, userId);
        
        // 扣款（先扣金幣）
        try {
            walletService.deductGold(
                    userId, 
                    pricePerDraw, 
                    "DRAW", 
                    lotteryId, 
                    "抽獎消費: " + lottery.getTitle()
            );
            
            // 記錄消費
            consumptionRecordService.recordConsumption(
                    userId,
                    "DRAW_GOLD",
                    lotteryId,
                    lottery.getTitle(),
                    null,
                    null,
                    pricePerDraw,
                    0L,
                    "金幣抽獎"
            );
            
            log.info("💰 扣款成功: userId={}, amount={}", userId, pricePerDraw);
        } catch (BusinessException e) {
            log.error("⚠️ 扣款失敗: {}", e.getMessage());
            throw e;
        }
        
        // 更新 Session 的開套者抽數和花費（只更新開套者）
        if (sessionInfo.isOpener()) {
            LotterySession session = lotterySessionMapper.selectByPrimaryKey(sessionInfo.sessionId());
            if (session != null) {
                session.setOpenerDrawCount((session.getOpenerDrawCount() != null ? session.getOpenerDrawCount() : 0) + 1);
                session.setOpenerTotalCost((session.getOpenerTotalCost() != null ? session.getOpenerTotalCost() : 0L) + pricePerDraw);
                session.setUpdatedAt(LocalDateTime.now());
                lotterySessionMapper.updateByPrimaryKey(session);
                
                // 檢查免單
                if (prizeId != null && isGrandPrize) {
                    triggeredFreeDraw = checkAndTriggerFreeDraw(sessionInfo.sessionId(), prizeId);
                    if (triggeredFreeDraw) {
                        refundAmount = session.getOpenerTotalCost();
                    }
                }
            }
        }
        
        // 9. 返回結果
        return new DrawResult(
                true, 
                targetTicket.getId(), 
                actualTicketNumber, 
                prizeId, 
                prizeLevel, 
                prizeName, 
                prizeImageUrl, 
                isGrandPrize, 
                triggeredFreeDraw,
                refundAmount,
                triggeredFreeDraw 
                        ? "恭喜中獲 " + prizeName + "！開套免單，退還 " + refundAmount + " 元！"
                        : "抽獎成功！恭喜獲得 " + prizeName
        );
    }

    @Override
    @Transactional
    public DrawResult drawByTicketId(String lotteryId, String userId, String ticketId) {
        log.info("🎰 執行指定票券抽獎: lotteryId={}, userId={}, ticketId={}", lotteryId, userId, ticketId);

        // 1. 檢查商品狀態
        Lottery lottery = lotteryMapper.selectByPrimaryKey(lotteryId);
        if (lottery == null) {
            return new DrawResult(false, ticketId, 0, null, null, null, null, false, false, 0L, "商品不存在");
        }
        if (!"ON_SHELF".equals(lottery.getStatus())) {
            return new DrawResult(false, ticketId, 0, null, null, null, null, false, false, 0L, "商品未上架");
        }

        // 2. 檢查保護時間
        if (!canDrawNow(lotteryId, userId)) {
            return new DrawResult(false, ticketId, 0, null, null, null, null, false, false, 0L,
                    "商品正在被其他玩家抽獎中，請稍後再試");
        }

        // 3. 查詢該票券
        LotteryTicket ticket = lotteryTicketMapper.selectByPrimaryKey(ticketId);
        if (ticket == null || ticket.getLotteryId() == null || !ticket.getLotteryId().equals(lotteryId)) {
            return new DrawResult(false, ticketId, 0, null, null, null, null, false, false, 0L, "該票券不存在於此抽獎活動");
        }
        if (!"AVAILABLE".equals(ticket.getStatus())) {
            return new DrawResult(false, ticketId, ticket.getTicketNumber() != null ? ticket.getTicketNumber() : 0,
                    null, null, null, null, false, false, 0L, "該票券已被抽走或不可用");
        }

        int actualTicketNumber = ticket.getTicketNumber() != null ? ticket.getTicketNumber() : 0;

        // 4. 更新籤位狀態為已抽
        ticket.setStatus("DRAWN");
        ticket.setDrawnBy(userId);
        ticket.setDrawnAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());
        lotteryTicketMapper.updateByPrimaryKey(ticket);

        log.info("✅ 籤位更新成功: ticketId={}, ticketNumber={}, prizeLevel={}", ticketId, actualTicketNumber, ticket.getPrizeLevel());

        // 5. 取得獎品資訊
        String prizeId = ticket.getPrizeId();
        String prizeLevel = ticket.getPrizeLevel();
        String prizeName = null;
        String prizeImageUrl = null;
        boolean isGrandPrize = false;

        if (prizeId != null) {
            LotteryPrize prize = lotteryPrizeMapper.selectByPrimaryKey(prizeId);
            if (prize != null) {
                prizeName = prize.getName();
                prizeImageUrl = prize.getImageUrl();
                isGrandPrize = prize.getIsGrandPrize() != null && prize.getIsGrandPrize() == 1;

                // 減少獎品剩餘數量
                if (prize.getRemaining() != null && prize.getRemaining() > 0) {
                    prize.setRemaining(prize.getRemaining() - 1);
                    prize.setUpdatedAt(LocalDateTime.now());
                    lotteryPrizeMapper.updateByPrimaryKey(prize);
                }
            }
        } else {
            prizeName = "謝謝惠顧";
        }

        // 6. 更新商品的 totalDraws
        if (lottery.getTotalDraws() == null) {
            lottery.setTotalDraws(1);
        } else {
            lottery.setTotalDraws(lottery.getTotalDraws() + 1);
        }
        lottery.setUpdatedAt(LocalDateTime.now());
        lotteryMapper.updateByPrimaryKey(lottery);

        // 7. 將獎品加入賞品盒（如果不是謝謝惠顧）
        if (prizeId != null) {
            try {
                Long recycleBonus = lottery.getPricePerDraw() != null ? lottery.getPricePerDraw() / 2 : 0L;
                prizeBoxService.addToPrizeBox(userId, lotteryId, prizeId, lottery.getStoreId(), recycleBonus);
                log.info("✅ 獎品已加入賞品盒: userId={}, prizeId={}", userId, prizeId);
            } catch (Exception e) {
                log.error("⚠️ 獎品加入賞品盒失敗: {}", e.getMessage());
            }
        }

        // 8. 扣款、記錄抽獎紀錄、檢查免單
        Long pricePerDraw = lottery.getPricePerDraw() != null ? lottery.getPricePerDraw() : 0L;
        boolean triggeredFreeDraw = false;
        Long refundAmount = 0L;
        
        // 取得或建立 Session
        SessionInfo sessionInfo = getOrCreateSession(lotteryId, userId);
        
        // 扣款（先扣金幣）
        try {
            walletService.deductGold(
                    userId, 
                    pricePerDraw, 
                    "DRAW", 
                    lotteryId, 
                    "抽獎消費: " + lottery.getTitle()
            );
            
            // 記錄消費
            consumptionRecordService.recordConsumption(
                    userId,
                    "DRAW_GOLD",
                    lotteryId,
                    lottery.getTitle(),
                    null,
                    null,
                    pricePerDraw,
                    0L,
                    "金幣抽獎"
            );
            
            log.info("💰 扣款成功: userId={}, amount={}", userId, pricePerDraw);
        } catch (BusinessException e) {
            log.error("⚠️ 扣款失敗: {}", e.getMessage());
            throw e;
        }
        
        // 更新 Session 的開套者抽數和花費（只更新開套者）
        if (sessionInfo.isOpener()) {
            LotterySession session = lotterySessionMapper.selectByPrimaryKey(sessionInfo.sessionId());
            if (session != null) {
                session.setOpenerDrawCount((session.getOpenerDrawCount() != null ? session.getOpenerDrawCount() : 0) + 1);
                session.setOpenerTotalCost((session.getOpenerTotalCost() != null ? session.getOpenerTotalCost() : 0L) + pricePerDraw);
                session.setUpdatedAt(LocalDateTime.now());
                lotterySessionMapper.updateByPrimaryKey(session);
                
                // 檢查免單
                if (prizeId != null && isGrandPrize) {
                    triggeredFreeDraw = checkAndTriggerFreeDraw(sessionInfo.sessionId(), prizeId);
                    if (triggeredFreeDraw) {
                        refundAmount = session.getOpenerTotalCost();
                    }
                }
            }
        }

        return new DrawResult(
                true,
                ticketId,
                actualTicketNumber,
                prizeId,
                prizeLevel,
                prizeName,
                prizeImageUrl,
                isGrandPrize,
                triggeredFreeDraw,
                refundAmount,
                triggeredFreeDraw 
                        ? "恭喜中獲 " + prizeName + "！開套免單，退還 " + refundAmount + " 元！"
                        : "抽獎成功！恭喜獲得 " + prizeName
        );
    }

    @Override
    public Integer getRandomAvailableTicket(String lotteryId) {
        // 查詢所有 AVAILABLE 狀態的籤位
        LotteryTicketExample example = new LotteryTicketExample();
        example.createCriteria()
                .andLotteryIdEqualTo(lotteryId)
                .andStatusEqualTo("AVAILABLE");
        List<LotteryTicket> availableTickets = lotteryTicketMapper.selectByExample(example);
        
        if (availableTickets.isEmpty()) {
            return null;
        }
        
        // 隨機選一個
        LotteryTicket selected = availableTickets.get(random.nextInt(availableTickets.size()));
        return selected.getTicketNumber();
    }

    // ==================== 開套場次管理 ====================

    @Override
    @Transactional
    public SessionInfo getOrCreateSession(String lotteryId, String userId) {
        log.info("🎭 查詢或建立場次: lotteryId={}, userId={}", lotteryId, userId);
        
        // 查詢是否有進行中的場次
        LotterySessionExample example = new LotterySessionExample();
        example.createCriteria()
                .andLotteryIdEqualTo(lotteryId)
                .andStatusEqualTo("ACTIVE");
        List<LotterySession> activeSessions = lotterySessionMapper.selectByExample(example);
        
        if (!activeSessions.isEmpty()) {
            // 有進行中的場次
            LotterySession activeSession = activeSessions.get(0);
            boolean isOpener = activeSession.getOpenerUserId().equals(userId);
            
            log.info("✅ 找到進行中場次: sessionId={}, isOpener={}", activeSession.getId(), isOpener);
            
            return new SessionInfo(
                    activeSession.getId(),
                    activeSession.getLotteryId(),
                    activeSession.getOpenerUserId(),
                    isOpener,
                    activeSession.getProtectionDraws() != null ? activeSession.getProtectionDraws() : 0,
                    activeSession.getProtectionEndTime(),
                    activeSession.getOpenerDrawCount() != null ? activeSession.getOpenerDrawCount() : 0,
                    activeSession.getFreeDrawEnabled() != null && activeSession.getFreeDrawEnabled() == 1,
                    activeSession.getStatus(),
                    activeSession.getPlayerDesignatedNumbers()  // 新增欄位
            );
        }
        
        // 建立新場次（當前使用者成為開套者）
        Lottery lottery = lotteryMapper.selectByPrimaryKey(lotteryId);
        if (lottery == null) {
            throw new BusinessException("抽獎活動不存在");
        }
        
        LotterySession newSession = new LotterySession();
        newSession.setId(UUID.randomUUID().toString());
        newSession.setLotteryId(lotteryId);
        newSession.setOpenerUserId(userId);
        newSession.setProtectionDraws(lottery.getProtectionDraws() != null ? lottery.getProtectionDraws() : 0);
        newSession.setProtectionStartTime(LocalDateTime.now());
        
        // 計算保護結束時間
        Integer protectionMinutes = lottery.getProtectionMinutes() != null ? lottery.getProtectionMinutes() : 5;
        newSession.setProtectionEndTime(LocalDateTime.now().plusMinutes(protectionMinutes));
        
        newSession.setOpenerDrawCount(0);
        newSession.setOpenerTotalCost(0L);
        newSession.setFreeDrawEnabled(lottery.getFreeDrawEnabled() != null ? lottery.getFreeDrawEnabled() : (byte) 0);
        newSession.setFreeDrawTriggered((byte) 0);
        newSession.setFreeDrawRefundAmount(0L);
        newSession.setStatus("ACTIVE");
        newSession.setCreatedAt(LocalDateTime.now());
        newSession.setUpdatedAt(LocalDateTime.now());
        
        lotterySessionMapper.insert(newSession);
        
        log.info("🆕 建立新場次: sessionId={}, opener={}, protectionEnd={}", 
                newSession.getId(), userId, newSession.getProtectionEndTime());
        
        return new SessionInfo(
                newSession.getId(),
                lotteryId,
                userId,
                true,
                newSession.getProtectionDraws(),
                newSession.getProtectionEndTime(),
                0,
                newSession.getFreeDrawEnabled() == 1,
                "ACTIVE",
                null  // 新建場次還未指定
        );
    }

    @Override
    @Transactional
    public boolean canDrawNow(String lotteryId, String userId) {
        log.info("🔍 檢查是否可抽獎: lotteryId={}, userId={}", lotteryId, userId);
        
        // 查詢是否有進行中的場次
        LotterySessionExample example = new LotterySessionExample();
        example.createCriteria()
                .andLotteryIdEqualTo(lotteryId)
                .andStatusEqualTo("ACTIVE");
        List<LotterySession> activeSessions = lotterySessionMapper.selectByExample(example);
        
        if (activeSessions.isEmpty()) {
            log.info("✅ 無進行中場次，可以抽獎");
            return true;
        }
        
        LotterySession activeSession = activeSessions.get(0);
        
        // 如果是開套者，永遠可以抽
        if (activeSession.getOpenerUserId().equals(userId)) {
            log.info("✅ 是開套者，可以抽獎");
            return true;
        }
        
        // 檢查保護時間是否已過
        LocalDateTime now = LocalDateTime.now();
        if (activeSession.getProtectionEndTime() != null && 
                activeSession.getProtectionEndTime().isBefore(now)) {
            // 保護時間已過，設為過期
            activeSession.setStatus("EXPIRED");
            activeSession.setCompletedAt(now);
            activeSession.setUpdatedAt(now);
            lotterySessionMapper.updateByPrimaryKey(activeSession);
            
            log.info("⏰ 保護時間已過，場次過期: sessionId={}", activeSession.getId());
            return true;
        }
        
        // 其他玩家且保護時間未過，不能抽
        log.warn("❌ 保護時間未過，其他玩家不能抽: opener={}, protectionEnd={}", 
                activeSession.getOpenerUserId(), activeSession.getProtectionEndTime());
        return false;
    }

    @Override
    public void expireOldSessions() {
        log.info("⏰ 清理過期場次...");
        // TODO: 定時任務呼叫
        // sessionMapper.expireByTime(LocalDateTime.now());
    }

    // ==================== 免單機制 ====================

    @Override
    @Transactional
    public boolean checkAndTriggerFreeDraw(String sessionId, String prizeId) {
        log.info("🎁 檢查免單機制: sessionId={}, prizeId={}", sessionId, prizeId);
        
        // 查詢 Session
        LotterySession session = lotterySessionMapper.selectByPrimaryKey(sessionId);
        if (session == null) {
            log.warn("⚠️ Session 不存在: {}", sessionId);
            return false;
        }
        
        // 檢查是否啟用免單
        if (session.getFreeDrawEnabled() == null || session.getFreeDrawEnabled() != 1) {
            log.info("❌ 未啟用免單機制");
            return false;
        }
        
        // 檢查是否已觸發過免單
        if (session.getFreeDrawTriggered() != null && session.getFreeDrawTriggered() == 1) {
            log.info("❌ 已觸發過免單");
            return false;
        }
        
        // 檢查是否在保護抽數內
        Integer openerDrawCount = session.getOpenerDrawCount() != null ? session.getOpenerDrawCount() : 0;
        Integer protectionDraws = session.getProtectionDraws() != null ? session.getProtectionDraws() : 0;
        
        if (openerDrawCount > protectionDraws) {
            log.info("❌ 超過保護抽數: {} > {}", openerDrawCount, protectionDraws);
            return false;
        }
        
        // 檢查是否中大獎
        if (prizeId == null) {
            log.info("❌ 未中獎（謝謝惠顧）");
            return false;
        }
        
        LotteryPrize prize = lotteryPrizeMapper.selectByPrimaryKey(prizeId);
        if (prize == null || prize.getIsGrandPrize() == null || prize.getIsGrandPrize() != 1) {
            log.info("❌ 不是大獎: prizeId={}", prizeId);
            return false;
        }
        
        // 觸發免單！
        Long refundAmount = session.getOpenerTotalCost() != null ? session.getOpenerTotalCost() : 0L;
        
        if (refundAmount > 0) {
            // 退款給使用者
            try {
                walletService.addGold(
                        session.getOpenerUserId(), 
                        refundAmount, 
                        "FREE_DRAW_REFUND", 
                        sessionId, 
                        "開套免單退款"
                );
                
                // 記錄退款消費
                Lottery lottery = lotteryMapper.selectByPrimaryKey(session.getLotteryId());
                consumptionRecordService.recordConsumption(
                        session.getOpenerUserId(),
                        "FREE_DRAW_REFUND",
                        session.getLotteryId(),
                        lottery != null ? lottery.getTitle() : null,
                        null,
                        null,
                        -refundAmount,  // 負數表示退款
                        0L,
                        "開套免單退款"
                );
                
                log.info("💰 退款成功: userId={}, amount={}", session.getOpenerUserId(), refundAmount);
            } catch (Exception e) {
                log.error("⚠️ 退款失敗: {}", e.getMessage(), e);
                return false;
            }
        }
        
        // 更新 Session 狀態
        session.setFreeDrawTriggered((byte) 1);
        session.setFreeDrawRefundAmount(refundAmount);
        session.setFreeDrawTriggeredAt(LocalDateTime.now());
        session.setFreeDrawPrizeId(prizeId);
        session.setUpdatedAt(LocalDateTime.now());
        lotterySessionMapper.updateByPrimaryKey(session);
        
        log.info("✅ 免單觸發成功: sessionId={}, refundAmount={}", sessionId, refundAmount);
        return true;
    }

    // ==================== 公共輔助方法 ====================

    @Override
    public List<Integer> getAvailableTicketNumbers(String lotteryId) {
        log.info("🔍 查詢可用籤位編號: lotteryId={}", lotteryId);
        
        LotteryTicketExample example = new LotteryTicketExample();
        example.createCriteria()
                .andLotteryIdEqualTo(lotteryId)
                .andStatusEqualTo("AVAILABLE");
        example.setOrderByClause("ticket_number ASC");
        
        List<LotteryTicket> tickets = lotteryTicketMapper.selectByExample(example);
        return tickets.stream()
                .map(LotteryTicket::getTicketNumber)
                .toList();
    }

    @Override
    public void markPlayerDesignated(String sessionId, List<Integer> prizeNumbers) {
        log.info("✏️ 標記玩家已指定大獎: sessionId={}, numbers={}", sessionId, prizeNumbers);
        
        LotterySession session = lotterySessionMapper.selectByPrimaryKey(sessionId);
        if (session != null) {
            // 將 List<Integer> 轉為 JSON 字串
            String numbersJson = prizeNumbers.toString();  // 簡單格式：[1,2,3]
            session.setPlayerDesignatedNumbers(numbersJson);
            session.setUpdatedAt(LocalDateTime.now());
            lotterySessionMapper.updateByPrimaryKey(session);
            
            log.info("✅ 標記完成");
        }
    }

    @Override
    public com.group.admin.entity.Lottery getLottery(String lotteryId) {
        return lotteryMapper.selectByPrimaryKey(lotteryId);
    }

    // ==================== 內部輔助 ====================

    private record PrizeSlot(String prizeId, String level) {}
}
