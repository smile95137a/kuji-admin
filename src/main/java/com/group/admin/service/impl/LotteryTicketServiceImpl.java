package com.group.admin.service.impl;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.group.admin.entity.Lottery;
import com.group.admin.entity.LotteryPrize;
import com.group.admin.entity.LotterySession;
import com.group.admin.entity.LotteryTicket;
import com.group.admin.enums.GameModeEnum;
import com.group.admin.example.LotteryPrizeExample;
import com.group.admin.example.LotterySessionExample;
import com.group.admin.example.LotteryTicketExample;
import com.group.admin.exception.BusinessException;
import com.group.admin.mapper.LotteryMapper;
import com.group.admin.mapper.LotteryPrizeMapper;
import com.group.admin.mapper.LotterySessionMapper;
import com.group.admin.mapper.LotteryTicketMapper;
import com.group.admin.res.lottery.LotteryTicketRes;
import com.group.admin.res.lottery.DesignationCheckResponse;
import com.group.admin.service.ConsumptionRecordService;
import com.group.admin.service.LotteryService;
import com.group.admin.service.LotteryTicketService;
import com.group.admin.service.PrizeBoxService;
import com.group.admin.service.SystemConfigService;
import com.group.admin.service.CoinService;

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
    private final CoinService walletService;
    private final ConsumptionRecordService consumptionRecordService;
    private final SystemConfigService systemConfigService;

    private final SecureRandom random = new SecureRandom();
    private final ConcurrentHashMap<String, Object> gachaLocks = new ConcurrentHashMap<>();

    @Lazy
    @Autowired
    private LotteryService lotteryService;

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
        
        // 建立獎品池（最後賞不加入籤位池，在最後一抽自動發放）
        List<PrizeSlot> prizePool = new ArrayList<>();
        for (LotteryPrize prize : prizes) {
            if (prize.getIsLastPrize() != null && prize.getIsLastPrize() == 1) {
                log.info("⏭️ 跳過最後賞（不加入籤位池）: {} (數量={})", prize.getName(), prize.getQuantity());
                continue;
            }
            int quantity = prize.getQuantity() != null ? prize.getQuantity() : 0;
            for (int i = 0; i < quantity; i++) {
                prizePool.add(new PrizeSlot(prize.getId(), prize.getLevel()));
            }
        }
        
        log.info("📦 獎品池大小: {}, 總籤位數: {}", prizePool.size(), totalTickets);
        
        // ⚠️ 一番賞/扭蛋/卡牌模式：非最後賞獎品總數必須 = 總籤位數
        if (prizePool.size() != totalTickets) {
            throw new BusinessException(
                String.format("一番賞/扭蛋/卡牌模式：非最後賞獎品總數(%d)必須等於總籤位數(%d)！",
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
        
        // 🆕 安全 log：避免 index out of bounds
        if (prizePool.size() == 1) {
            log.info("✅ 隨機籤位生成完成，獎品分配範例: {}個籤位均分配獎品", totalTickets);
        } else if (prizePool.size() >= 2) {
            log.info("✅ 隨機籤位生成完成，獎品分配範例: 1號={}, 2號={}, ...{}號={}", 
                    prizePool.get(0).level(), 
                    prizePool.get(1).level(),
                    totalTickets,
                    prizePool.get(totalTickets - 1).level());
        }
    }

    /**
     * 生成刮刮樂籤位（雙號碼機制）
     *
     * <p>設計原則：</p>
     * <ul>
     *   <li>ticket_number = 實體卡物理序號（1-N）</li>
     *   <li>revealed_number = 刮開後顯示的亂數號碼（shuffle [1..N] 分配）</li>
     *   <li>SCRATCH_STORE：店家指定哪些 revealed_number 是大獎</li>
     *   <li>SCRATCH_PLAYER：專建立 revealed_number，等開套玩家呼叫 /designate 指定</li>
     * </ul>
     */
    private void generateScratchTickets(String lotteryId, int totalTickets, Lottery lottery) {
        log.info("🎫 生成刮刮樂籤位(雙號碼機制): lotteryId={}, total={}", lotteryId, totalTickets);

        // 步驔1: 建立 1..N 的 revealed_number 列表並打亂
        List<Integer> revealedNumbers = new ArrayList<>();
        for (int i = 1; i <= totalTickets; i++) revealedNumbers.add(i);
        Collections.shuffle(revealedNumbers, random);

        // 步驔2: 收集大獎對應的 revealed_number 集合
        // 只有 SCRATCH_STORE 在建立時解析店家預設的得獎號碼
        // SCRATCH_PLAYER 此時 designated_prize_numbers 為 null，等開套玩家呼叫 /designate 再指定
        Set<Integer> winningRevealedNumbers = new HashSet<>();
        String gameMode = lottery.getGameMode(); // SCRATCH_STORE / SCRATCH_PLAYER / RANDOM
        if (GameModeEnum.SCRATCH_STORE.getCode().equals(gameMode)) {
            String designatedJson = lottery.getDesignatedPrizeNumbers();
            if (designatedJson != null && !designatedJson.trim().isEmpty()) {
                String cleaned = designatedJson.trim().replaceAll("[\\[\\]\\s]", "");
                for (String numStr : cleaned.split(",")) {
                    try { winningRevealedNumbers.add(Integer.parseInt(numStr.trim())); }
                    catch (NumberFormatException ignored) {}
                }
            }
        }

        // 步驔3: 取得獎品清單，建立 revealed_number → prize 映射
        LotteryPrizeExample prizeExample = new LotteryPrizeExample();
        prizeExample.createCriteria().andLotteryIdEqualTo(lotteryId);
        prizeExample.setOrderByClause("order_num ASC");
        List<LotteryPrize> prizes = lotteryPrizeMapper.selectByExample(prizeExample);

        List<PrizeSlot> prizePool = new ArrayList<>();
        for (LotteryPrize prize : prizes) {
            int quantity = prize.getQuantity() != null ? prize.getQuantity() : 0;
            for (int i = 0; i < quantity; i++) {
                prizePool.add(new PrizeSlot(prize.getId(), prize.getLevel()));
            }
        }

        // 將大獎 revealed_number 與獎品池一一對應
        Map<Integer, PrizeSlot> revealedToPrize = new HashMap<>();
        Iterator<PrizeSlot> prizeIter = prizePool.iterator();
        for (Integer winNum : winningRevealedNumbers) {
            if (prizeIter.hasNext()) {
                revealedToPrize.put(winNum, prizeIter.next());
            }
        }

        // 步驔4: 生成所有籤位
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < totalTickets; i++) {
            int revealedNumber = revealedNumbers.get(i);
            PrizeSlot slot = revealedToPrize.get(revealedNumber);

            LotteryTicket ticket = new LotteryTicket();
            ticket.setId(UUID.randomUUID().toString());
            ticket.setLotteryId(lotteryId);
            ticket.setTicketNumber(i + 1);              // 物理序號
            ticket.setRevealedNumber(revealedNumber);   // 刮開後顯示的亂數號碼
            ticket.setPrizeId(slot != null ? slot.prizeId() : null);
            ticket.setPrizeLevel(slot != null ? slot.level() : "THANKS");
            ticket.setStatus("AVAILABLE");
            ticket.setIsDesignatedPrize(slot != null ? (byte) 1 : (byte) 0);
            ticket.setDesignatedBy(slot != null ? "STORE" : null);
            ticket.setCreatedAt(now);
            ticket.setUpdatedAt(now);
            lotteryTicketMapper.insert(ticket);
        }

        log.info("✅ 刮刮樂籤位生成完成, 大獎 revealed_number: {}, 謝謝惠顧: {}",
                winningRevealedNumbers, totalTickets - winningRevealedNumbers.size());

        // SCRATCH_STORE: 店家開獎後，將剩餘籤位自動分配非大獎獎品
        if (GameModeEnum.SCRATCH_STORE.getCode().equals(gameMode) && !winningRevealedNumbers.isEmpty()) {
            autoAssignNonGrandPrizes(lotteryId);
        }
    }

    @Override
    @Transactional
    public void designatePrizePositions(String lotteryId, String userId, List<LotteryTicketService.PrizeDesignation> designations) {
        log.info("🎯 開套玩家指定大獎位置: lotteryId={}, userId={}, designations={}", 
                lotteryId, userId, designations);
        
        if (designations == null || designations.isEmpty()) {
            throw new BusinessException("請指定大獎位置");
        }
        
        // 驗證每個指定是否有效
        for (LotteryTicketService.PrizeDesignation designation : designations) {
            Integer revealedNumber = designation.revealedNumber();
            String prizeId = designation.prizeId();
            
            if (revealedNumber == null || prizeId == null) {
                throw new BusinessException("揭露號碼和獎品 ID 不可為空");
            }
            
            // 驗證獎品是否存在且為大獎
            LotteryPrize prize = lotteryPrizeMapper.selectByPrimaryKey(prizeId);
            if (prize == null) {
                throw new BusinessException("獎品不存在: " + prizeId);
            }
            if (!lotteryId.equals(prize.getLotteryId())) {
                throw new BusinessException("獎品不屬於此商品");
            }
            if (prize.getIsGrandPrize() == null || prize.getIsGrandPrize() != 1) {
                throw new BusinessException("獎品 " + prize.getName() + " 不是大獎");
            }
            
            // 根據 revealed_number 查找對應籤位
            LotteryTicketExample ticketExample = new LotteryTicketExample();
            ticketExample.createCriteria()
                    .andLotteryIdEqualTo(lotteryId)
                    .andStatusEqualTo("AVAILABLE");
            List<LotteryTicket> allTickets = lotteryTicketMapper.selectByExample(ticketExample);
            LotteryTicket target = allTickets.stream()
                    .filter(t -> revealedNumber.equals(t.getRevealedNumber()))
                    .findFirst()
                    .orElse(null);

            if (target == null) {
                throw new BusinessException("revealed_number #" + revealedNumber + " 不存在或已被抽走");
            }
            
            // 更新籤位為指定的大獎
            target.setPrizeId(prizeId);
            target.setPrizeLevel(prize.getLevel());
            target.setIsDesignatedPrize((byte) 1);
            target.setDesignatedBy("PLAYER");
            target.setUpdatedAt(LocalDateTime.now());
            lotteryTicketMapper.updateByPrimaryKey(target);
            
            log.info("✅ revealed_number #{} 指定為 {} ({})", revealedNumber, prize.getName(), prize.getLevel());
        }
        
        // 標記 Session 已指定
        SessionInfo session = getOrCreateSession(lotteryId, userId);
        List<Integer> numbers = designations.stream()
                .map(LotteryTicketService.PrizeDesignation::revealedNumber)
                .toList();
        markPlayerDesignated(session.sessionId(), numbers);
        
        // 大獎指定完成後，自動隨機分配剩餘非大獎獎品
        autoAssignNonGrandPrizes(lotteryId);

        log.info("✅ 大獎位置指定完成，共 {} 個，非大獎已自動隨機分配", designations.size());
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
                res.setRevealedNumber(null);          // ← 刮刮樂：隱藏刮開號碼（安全關鍵！）
            }
            result.add(res);
        }
        
        result.sort(java.util.Comparator.comparing(
                LotteryTicketRes::getTicketNumber,
                java.util.Comparator.nullsLast(Integer::compareTo)
        ));
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
        res.setRevealedNumber(ticket.getRevealedNumber());  // 刮刮樂专用；一番賞/扭蛋為 null
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
            return new DrawResult(false, null, 0, null, null, null, null, null, false, false, 0L, "商品不存在", false, null, null, null);
        }
        if (!"ON_SHELF".equals(lottery.getStatus())) {
            return new DrawResult(false, null, 0, null, null, null, null, null, false, false, 0L, "商品未上架", false, null, null, null);
        }
        
        // 2. 扭蛋不需要保護時間（在 Controller 層使用 synchronized）；其他類別檢查保護時間
        boolean isGacha = "GACHA".equals(lottery.getCategory());
        if (!isGacha && !canDrawNow(lotteryId, userId)) {
            return new DrawResult(false, null, 0, null, null, null, null, null, false, false, 0L, 
                    "商品正在被其他玩家抽獎中，請稍後再試", false, null, null, null);
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
                return new DrawResult(false, null, 0, null, null, null, null, null, false, false, 0L, 
                        "該籤位已被抽走或不存在", false, null, null, null);
            }
            targetTicket = tickets.get(0);
            actualTicketNumber = ticketNumber;
        } else {
            // 隨機模式
            Integer randomNumber = getRandomAvailableTicket(lotteryId);
            if (randomNumber == null) {
                return new DrawResult(false, null, 0, null, null, null, null, null, false, false, 0L, "已無可抽籤位", false, null, null, null);
            }
            
            // 取得該籤位
            LotteryTicketExample example = new LotteryTicketExample();
            example.createCriteria()
                    .andLotteryIdEqualTo(lotteryId)
                    .andTicketNumberEqualTo(randomNumber);
            List<LotteryTicket> tickets = lotteryTicketMapper.selectByExample(example);
            
            if (tickets.isEmpty()) {
                return new DrawResult(false, null, 0, null, null, null, null, null, false, false, 0L, "籤位查詢失敗", false, null, null, null);
            }
            targetTicket = tickets.get(0);
            actualTicketNumber = randomNumber;
        }
        
        // 4. 以原子條件更新避免併發下重複抽到同一籤位
        LocalDateTime drawTime = LocalDateTime.now();
        LotteryTicket claimRow = new LotteryTicket();
        claimRow.setStatus("DRAWN");
        claimRow.setDrawnBy(userId);
        claimRow.setDrawnAt(drawTime);
        claimRow.setUpdatedAt(drawTime);

        LotteryTicketExample claimExample = new LotteryTicketExample();
        claimExample.createCriteria()
            .andIdEqualTo(targetTicket.getId())
            .andStatusEqualTo("AVAILABLE");
        int affectedRows = lotteryTicketMapper.updateByExampleSelective(claimRow, claimExample);
        if (affectedRows == 0) {
            return new DrawResult(false, null, 0, null, null, null, null, null, false, false, 0L,
                "該籤位已被其他玩家搶先抽走，請重試", false, null, null, null);
        }

        targetTicket.setStatus("DRAWN");
        targetTicket.setDrawnBy(userId);
        targetTicket.setDrawnAt(drawTime);
        targetTicket.setUpdatedAt(drawTime);
        
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
        
        // 🆕 非扭蛋：首次抽獎時啟動保護時間
        if (!isGacha && sessionInfo.protectionEndTime() == null) {
            startProtection(sessionInfo.sessionId(), lotteryId);
            log.info("🛡️ 保護時間已啟動: sessionId={}", sessionInfo.sessionId());
        } else if (!isGacha && sessionInfo.protectionEndTime() != null && sessionInfo.isOpener()) {
            extendProtection(sessionInfo.sessionId());
        }
        
        String paymentType = resolvePaymentType(lottery);

        // 扣款（依 paymentType 決定扣金幣或紅利，不做混合扣款）
        try {
            if ("BONUS".equals(paymentType)) {
                walletService.deductBonus(userId, pricePerDraw, "DRAW", lotteryId,
                        "抽獎消費: " + lottery.getTitle());
                consumptionRecordService.recordConsumption(
                        userId, "DRAW_BONUS", lotteryId, lottery.getTitle(),
                        null, null, 0L, pricePerDraw, "紅利抽獎");
            } else {
                walletService.deductGold(userId, pricePerDraw, "DRAW", lotteryId,
                        "抽獎消費: " + lottery.getTitle());
                consumptionRecordService.recordConsumption(
                        userId, "DRAW_GOLD", lotteryId, lottery.getTitle(),
                        null, null, pricePerDraw, 0L, "金幣抽獎");
            }
            log.info("💰 扣款成功: userId={}, amount={}, paymentType={}", userId, pricePerDraw, paymentType);
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
        
        // 9. 最後賞檢查（全套票抽完時自動發放）
        List<LotteryPrize> lastPrizesAwarded = checkAndAwardLastPrize(lottery, userId);
        boolean lastPrizeAwarded = !lastPrizesAwarded.isEmpty();
        String lastPrizeId = null;
        String lastPrizeName = null;
        String lastPrizeImageUrl = null;
        if (lastPrizeAwarded) {
            LotteryPrize first = lastPrizesAwarded.get(0);
            lastPrizeId = first.getId();
            lastPrizeName = first.getName();
            lastPrizeImageUrl = first.getImageUrl();
        }

        // 10. 返回結果
        DrawResult result = new DrawResult(
                true, 
                targetTicket.getId(), 
                actualTicketNumber,
                targetTicket.getRevealedNumber(),  // 刮刮樂專用；一番賞/扭蛋為 null
                prizeId, 
                prizeLevel, 
                prizeName, 
                prizeImageUrl, 
                isGrandPrize, 
                triggeredFreeDraw,
                refundAmount,
                triggeredFreeDraw 
                        ? "恭喜中獲 " + prizeName + "！開套免單，退還 " + refundAmount + " 元！"
                        : "抽獎成功！恭喜獲得 " + prizeName,
                lastPrizeAwarded,
                lastPrizeId,
                lastPrizeName,
                lastPrizeImageUrl
        );

        // T016: 自動下架檢查
        lotteryService.checkAndDelist(lotteryId);

        return result;
    }

    @Override
    @Transactional
    public DrawResult drawByTicketId(String lotteryId, String userId, String ticketId) {
        log.info("🎰 執行指定票券抽獎: lotteryId={}, userId={}, ticketId={}", lotteryId, userId, ticketId);

        // 1. 檢查商品狀態
        Lottery lottery = lotteryMapper.selectByPrimaryKey(lotteryId);
        if (lottery == null) {
            return new DrawResult(false, ticketId, 0, null, null, null, null, null, false, false, 0L, "商品不存在", false, null, null, null);
        }
        if (!"ON_SHELF".equals(lottery.getStatus())) {
            return new DrawResult(false, ticketId, 0, null, null, null, null, null, false, false, 0L, "商品未上架", false, null, null, null);
        }

        // 2. 扭蛋不需要保護時間（在 Controller 層使用 synchronized）；其他類別檢查保護時間
        boolean isGacha = "GACHA".equals(lottery.getCategory());
        if (!isGacha && !canDrawNow(lotteryId, userId)) {
            return new DrawResult(false, ticketId, 0, null, null, null, null, null, false, false, 0L,
                    "商品正在被其他玩家抽獎中，請稍後再試", false, null, null, null);
        }

        // 3. 查詢該票券
        LotteryTicket ticket = lotteryTicketMapper.selectByPrimaryKey(ticketId);
        if (ticket == null || ticket.getLotteryId() == null || !ticket.getLotteryId().equals(lotteryId)) {
            return new DrawResult(false, ticketId, 0, null, null, null, null, null, false, false, 0L, "該票券不存在於此抽獎活動", false, null, null, null);
        }
        if (!"AVAILABLE".equals(ticket.getStatus())) {
            return new DrawResult(false, ticketId, ticket.getTicketNumber() != null ? ticket.getTicketNumber() : 0,
                    null, null, null, null, null, false, false, 0L, "該票券已被抽走或不可用", false, null, null, null);
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
        
        // 🆕 非扭蛋：首次抽獎時啟動保護時間
        if (!isGacha && sessionInfo.protectionEndTime() == null) {
            startProtection(sessionInfo.sessionId(), lotteryId);
            log.info("🛡️ 保護時間已啟動: sessionId={}", sessionInfo.sessionId());
        } else if (!isGacha && sessionInfo.protectionEndTime() != null && sessionInfo.isOpener()) {
            extendProtection(sessionInfo.sessionId());
        }
        
        String paymentType = resolvePaymentType(lottery);

        // 扣款（依 paymentType 決定扣金幣或紅利，不做混合扣款）
        try {
            if ("BONUS".equals(paymentType)) {
                walletService.deductBonus(userId, pricePerDraw, "DRAW", lotteryId,
                        "抽獎消費: " + lottery.getTitle());
                consumptionRecordService.recordConsumption(
                        userId, "DRAW_BONUS", lotteryId, lottery.getTitle(),
                        null, null, 0L, pricePerDraw, "紅利抽獎");
            } else {
                walletService.deductGold(userId, pricePerDraw, "DRAW", lotteryId,
                        "抽獎消費: " + lottery.getTitle());
                consumptionRecordService.recordConsumption(
                        userId, "DRAW_GOLD", lotteryId, lottery.getTitle(),
                        null, null, pricePerDraw, 0L, "金幣抽獎");
            }
            log.info("💰 扣款成功: userId={}, amount={}, paymentType={}", userId, pricePerDraw, paymentType);
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

        // 最後賞檢查（全套票抽完時自動發放）
        List<LotteryPrize> lastPrizesAwarded = checkAndAwardLastPrize(lottery, userId);
        boolean lastPrizeAwarded = !lastPrizesAwarded.isEmpty();
        String lastPrizeId = null;
        String lastPrizeName = null;
        String lastPrizeImageUrl = null;
        if (lastPrizeAwarded) {
            LotteryPrize first = lastPrizesAwarded.get(0);
            lastPrizeId = first.getId();
            lastPrizeName = first.getName();
            lastPrizeImageUrl = first.getImageUrl();
        }

        DrawResult resultByTicketId = new DrawResult(
                true,
                ticketId,
                actualTicketNumber,
                ticket.getRevealedNumber(),   // 刮刮樂專用；一番賞/扭蛋為 null
                prizeId,
                prizeLevel,
                prizeName,
                prizeImageUrl,
                isGrandPrize,
                triggeredFreeDraw,
                refundAmount,
                triggeredFreeDraw 
                        ? "恭喜中獲 " + prizeName + "！開套免單，退還 " + refundAmount + " 元！"
                        : "抽獎成功！恭喜獲得 " + prizeName,
                lastPrizeAwarded,
                lastPrizeId,
                lastPrizeName,
                lastPrizeImageUrl
        );

        // T016: 自動下架檢查
        lotteryService.checkAndDelist(lotteryId);

        return resultByTicketId;
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

    // ==================== 最後賞自動發放 ====================

    /**
     * 最後一抽後，檢查是否需要自動發放最後賞。
     * 當全套所有 AVAILABLE 籤位已抽完，且商品設有 isLastPrize=1 的獎品時自動觸發。
     *
     * @param lottery     商品
     * @param lastUserId  觸發此次抽獎的用戶 ID（最後賞發放對象）
     * @return 已發放的最後賞列表（未觸發則為空列表）
     */
    private List<LotteryPrize> checkAndAwardLastPrize(Lottery lottery, String lastUserId) {
        // 1. 確認是否還有 AVAILABLE 籤位
        LotteryTicketExample availableCheck = new LotteryTicketExample();
        availableCheck.createCriteria()
                .andLotteryIdEqualTo(lottery.getId())
                .andStatusEqualTo("AVAILABLE");
        long remainingCount = lotteryTicketMapper.countByExample(availableCheck);

        if (remainingCount > 0) {
            return List.of(); // 還有籤位，不觸發最後賞
        }

        // 2. 查詢此商品的所有最後賞獎品
        LotteryPrizeExample lastPrizeExample = new LotteryPrizeExample();
        lastPrizeExample.createCriteria()
                .andLotteryIdEqualTo(lottery.getId())
                .andIsLastPrizeEqualTo((byte) 1);
        List<LotteryPrize> lastPrizes = lotteryPrizeMapper.selectByExample(lastPrizeExample);

        if (lastPrizes.isEmpty()) {
            return List.of(); // 此商品沒有最後賞
        }

        Long recycleBonus = lottery.getPricePerDraw() != null ? lottery.getPricePerDraw() / 2 : 0L;
        List<LotteryPrize> awarded = new ArrayList<>();

        for (LotteryPrize lastPrize : lastPrizes) {
            int qty = lastPrize.getQuantity() != null ? lastPrize.getQuantity() : 1;
            for (int i = 0; i < qty; i++) {
                try {
                    prizeBoxService.addToPrizeBox(lastUserId, lottery.getId(), lastPrize.getId(),
                            lottery.getStoreId(), recycleBonus);
                } catch (Exception e) {
                    log.error("⚠️ 最後賞加入賞品盒失敗: prizeId={}, error={}", lastPrize.getId(), e.getMessage());
                }
            }
            // 更新最後賞 remaining 為 0
            lastPrize.setRemaining(0);
            lastPrize.setUpdatedAt(LocalDateTime.now());
            lotteryPrizeMapper.updateByPrimaryKey(lastPrize);

            awarded.add(lastPrize);
            log.info("🏆 最後賞自動發放: userId={}, prizeId={}, prizeName={}, qty={}",
                    lastUserId, lastPrize.getId(), lastPrize.getName(), qty);
        }

        return awarded;
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
            
            // 🛡️ 保護時間逾期檢查：保護時間已到且非開套者 → 自動 EXPIRED，讓當前使用者成為新開套者
            LocalDateTime now = LocalDateTime.now();
            boolean isProtectionExpired = activeSession.getProtectionEndTime() != null
                    && activeSession.getProtectionEndTime().isBefore(now)
                    && !activeSession.getOpenerUserId().equals(userId);
            if (isProtectionExpired) {
                log.info("⏰ getOrCreateSession：保護時間已過，釋放舊場次: sessionId={}, protectionEnd={}",
                        activeSession.getId(), activeSession.getProtectionEndTime());
                activeSession.setStatus("EXPIRED");
                activeSession.setCompletedAt(now);
                activeSession.setUpdatedAt(now);
                lotterySessionMapper.updateByPrimaryKey(activeSession);
                // 繼續往下建立新場次
            } else {
            // 🆕 SCRATCH_PLAYER 逾時檢查：若開套者未在 10 分鐘內指定大獎，自動釋放場次
            Lottery lotteryForTimeout = lotteryMapper.selectByPrimaryKey(lotteryId);
                boolean isTimedOut = lotteryForTimeout != null
                    && GameModeEnum.SCRATCH_PLAYER.getCode().equals(lotteryForTimeout.getGameMode())
                    && activeSession.getDesignationDeadline() != null
                    && activeSession.getDesignationDeadline().isBefore(LocalDateTime.now())
                    && (activeSession.getPlayerDesignatedNumbers() == null
                            || activeSession.getPlayerDesignatedNumbers().isBlank());
            
            if (!isTimedOut) {
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
                        activeSession.getPlayerDesignatedNumbers(),
                        activeSession.getDesignationDeadline()  // 🆕
                );
            }
            
            // 指定逾時：標記舊場次 EXPIRED，讓當前使用者成為新開套者
            log.info("⏰ 開套者指定逾時，自動釋放場次: sessionId={}, 期限={}", 
                    activeSession.getId(), activeSession.getDesignationDeadline());
            activeSession.setStatus("EXPIRED");
            activeSession.setCompletedAt(LocalDateTime.now());
            activeSession.setUpdatedAt(LocalDateTime.now());
            lotterySessionMapper.updateByPrimaryKey(activeSession);
            // 繼續往下建立新場次（當前使用者成為新開套者）
            } // end else (isProtectionExpired)
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
        // 🆕 建立場次時不設定保護時間，等實際抽獎時才啟動（由 startProtection 處理）
        newSession.setProtectionStartTime(null);
        newSession.setProtectionEndTime(null);
        
        newSession.setOpenerDrawCount(0);
        newSession.setOpenerTotalCost(0L);
        newSession.setFreeDrawEnabled(lottery.getFreeDrawEnabled() != null ? lottery.getFreeDrawEnabled() : (byte) 0);
        newSession.setFreeDrawTriggered((byte) 0);
        newSession.setFreeDrawRefundAmount(0L);
        newSession.setStatus("ACTIVE");
        newSession.setCreatedAt(LocalDateTime.now());
        newSession.setUpdatedAt(LocalDateTime.now());
        
        // 🆕 SCRATCH_PLAYER 模式：設定 10 分鐘大獎指定截止時間
        if (GameModeEnum.SCRATCH_PLAYER.getCode().equals(lottery.getGameMode())) {
            newSession.setDesignationDeadline(LocalDateTime.now().plusMinutes(10));
            log.info("⏱️ SCRATCH_PLAYER：設定指定截止時間 = {}", newSession.getDesignationDeadline());
        }
        
        lotterySessionMapper.insert(newSession);
        
        log.info("🆕 建立新場次（保護時間待啟動）: sessionId={}, opener={}", 
                newSession.getId(), userId);
        
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
                null,  // 新建場次還未指定
                newSession.getDesignationDeadline()  // 🆕
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
        
        // 🆕 保護時間未啟動（尚未有人抽獎）→ 允許
        if (activeSession.getProtectionEndTime() == null) {
            log.info("✅ 保護時間尚未啟動，可以抽獎");
            return true;
        }
        
        // 檢查保護時間是否已過
        LocalDateTime now = LocalDateTime.now();
        if (activeSession.getProtectionEndTime().isBefore(now)) {
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

    @Override
    public List<Integer> getAvailableRevealedNumbers(String lotteryId) {
        log.info("🔍 查詢可用 revealedNumber 清單: lotteryId={}", lotteryId);

        LotteryTicketExample example = new LotteryTicketExample();
        example.createCriteria()
                .andLotteryIdEqualTo(lotteryId)
                .andStatusEqualTo("AVAILABLE");
        example.setOrderByClause("revealed_number ASC");

        return lotteryTicketMapper.selectByExample(example).stream()
                .map(LotteryTicket::getRevealedNumber)
                .distinct()
                .toList();
    }

    @Override
    public com.group.admin.res.lottery.TicketListResponse getTicketList(String lotteryId) {
        log.info("🎫 查詢籤位列表: lotteryId={}", lotteryId);

        Lottery lottery = lotteryMapper.selectByPrimaryKey(lotteryId);

        LotteryTicketExample example = new LotteryTicketExample();
        example.createCriteria().andLotteryIdEqualTo(lotteryId);
        example.setOrderByClause("ticket_number ASC");
        List<LotteryTicket> tickets = lotteryTicketMapper.selectByExample(example);

        long availableCount = tickets.stream().filter(t -> "AVAILABLE".equals(t.getStatus())).count();
        long drawnCount = tickets.stream().filter(t -> "DRAWN".equals(t.getStatus())).count();

        List<com.group.admin.res.lottery.TicketListResponse.TicketView> views = tickets.stream().map(t -> {
            if ("AVAILABLE".equals(t.getStatus())) {
                return com.group.admin.res.lottery.TicketListResponse.TicketView.builder()
                        .id(t.getId())
                        .ticketNumber(t.getTicketNumber())
                        .status(t.getStatus())
                        .build();
            } else {
                String prizeName = null;
                String prizeLevel = null;
                String prizeImageUrl = null;
                Boolean isGrandPrize = null;
                if (t.getPrizeId() != null) {
                    LotteryPrize p = lotteryPrizeMapper.selectByPrimaryKey(t.getPrizeId());
                    if (p != null) {
                        prizeName = p.getName();
                        prizeLevel = p.getLevel();
                        prizeImageUrl = p.getImageUrl();
                        isGrandPrize = p.getIsGrandPrize() != null && p.getIsGrandPrize() == 1;
                    }
                }
                return com.group.admin.res.lottery.TicketListResponse.TicketView.builder()
                        .id(t.getId())
                        .ticketNumber(t.getTicketNumber())
                        .status(t.getStatus())
                        .revealedNumber(t.getRevealedNumber())
                        .prizeId(t.getPrizeId())
                        .prizeName(prizeName)
                        .prizeLevel(prizeLevel)
                        .prizeImageUrl(prizeImageUrl)
                        .isGrandPrize(isGrandPrize)
                        .build();
            }
        }).sorted(java.util.Comparator.comparing(
                com.group.admin.res.lottery.TicketListResponse.TicketView::getTicketNumber,
                java.util.Comparator.nullsLast(Integer::compareTo)
        )).collect(Collectors.toList());

        return com.group.admin.res.lottery.TicketListResponse.builder()
                .lotteryId(lotteryId)
                .gameMode(lottery != null ? lottery.getGameMode() : null)
                .totalTickets(tickets.size())
                .availableCount((int) availableCount)
                .drawnCount((int) drawnCount)
                .tickets(views)
                .build();
    }

    @Override
    public List<com.group.admin.entity.LotteryPrize> getGrandPrizes(String lotteryId) {
        log.info("🏆 查詢大獎清單: lotteryId={}", lotteryId);

        LotteryPrizeExample example = new LotteryPrizeExample();
        example.createCriteria()
                .andLotteryIdEqualTo(lotteryId)
                .andIsGrandPrizeEqualTo((byte) 1);
        example.setOrderByClause("order_num ASC");
        return lotteryPrizeMapper.selectByExample(example);
    }

    /**
     * 自動將非大獎獎品隨機分配至尚未指定獎品的 AVAILABLE 籤位。
     * <p>
     * 條件：prize_id IS NULL AND is_designated_prize = 0 AND status = AVAILABLE
     * 流程：
     *   1. 查詢所有非大獎（isGrandPrize != 1）及其數量，展開成 prizeId 列表
     *   2. 查詢尚未分配的籤位
     *   3. 打亂獎品列表，逐一指定
     *   4. 多餘或不足以賞惠顧落地
     */
    private void autoAssignNonGrandPrizes(String lotteryId) {
        log.info("🎲 自動分配非大獎獎品: lotteryId={}", lotteryId);

        // 1. 查詢非大獎獎品
        LotteryPrizeExample prizeEx = new LotteryPrizeExample();
        LotteryPrizeExample.Criteria prizeCriteria = prizeEx.createCriteria()
                .andLotteryIdEqualTo(lotteryId);
        // 排除大獎（isGrandPrize = 1）
        List<LotteryPrize> allPrizes = lotteryPrizeMapper.selectByExample(prizeEx);
        List<PrizeSlot> nonGrandPool = new ArrayList<>();
        for (LotteryPrize prize : allPrizes) {
            if (prize.getIsGrandPrize() != null && prize.getIsGrandPrize() == 1) continue;
            if ("THANKS".equalsIgnoreCase(prize.getLevel())) continue; // 謝謝惠顧本來就是 null prize
            int qty = prize.getQuantity() != null ? prize.getQuantity() : 0;
            for (int i = 0; i < qty; i++) {
                nonGrandPool.add(new PrizeSlot(prize.getId(), prize.getLevel()));
            }
        }
        Collections.shuffle(nonGrandPool, random);

        // 2. 查詢未指定 prizeId 的 AVAILABLE 籤位（prizeId 為 null 且非大獎）
        LotteryTicketExample ticketEx = new LotteryTicketExample();
        ticketEx.createCriteria()
                .andLotteryIdEqualTo(lotteryId)
                .andStatusEqualTo("AVAILABLE")
                .andIsDesignatedPrizeEqualTo((byte) 0);
        ticketEx.setOrderByClause("ticket_number ASC");
        List<LotteryTicket> unassigned = lotteryTicketMapper.selectByExample(ticketEx);

        log.info("🎲 非大獎池: {} 個, 未分配籤位: {} 個", nonGrandPool.size(), unassigned.size());

        // 3. 逐一分配
        int assignCount = Math.min(nonGrandPool.size(), unassigned.size());
        for (int i = 0; i < assignCount; i++) {
            LotteryTicket ticket = unassigned.get(i);
            PrizeSlot slot = nonGrandPool.get(i);
            ticket.setPrizeId(slot.prizeId());
            ticket.setPrizeLevel(slot.level());
            ticket.setUpdatedAt(LocalDateTime.now());
            lotteryTicketMapper.updateByPrimaryKey(ticket);
        }

        log.info("✅ 非大獎自動分配完成: {} 個籤位已分配獎品，剩餘 {} 個籤位保持謝謝惠顧",
                assignCount, unassigned.size() - assignCount);
    }

    private String resolvePaymentType(Lottery lottery) {
        return "BONUS".equalsIgnoreCase(lottery.getPaymentType()) ? "BONUS" : "GOLD";
    }

    // ==================== 內部輔助 ====================

    private record PrizeSlot(String prizeId, String level) {}

    // ==================== 新增方法（2026-03-02）====================

    @Override
    public SessionInfo getActiveSession(String lotteryId, String userId) {
        log.info("🔍 唯讀查詢進行中場次: lotteryId={}", lotteryId);
        
        LotterySessionExample example = new LotterySessionExample();
        example.createCriteria()
                .andLotteryIdEqualTo(lotteryId)
                .andStatusEqualTo("ACTIVE");
        List<LotterySession> sessions = lotterySessionMapper.selectByExample(example);
        
        if (sessions.isEmpty()) {
            log.info("ℹ️ 無進行中場次");
            return null;
        }
        
        LotterySession session = sessions.get(0);
        
        // 如果保護時間已過，自動過期
        if (session.getProtectionEndTime() != null && session.getProtectionEndTime().isBefore(LocalDateTime.now())) {
            session.setStatus("EXPIRED");
            session.setCompletedAt(LocalDateTime.now());
            session.setUpdatedAt(LocalDateTime.now());
            lotterySessionMapper.updateByPrimaryKey(session);
            log.info("⏰ 場次保護時間已過，自動過期: sessionId={}", session.getId());
            return null;
        }
        
        boolean isOpener = userId != null && userId.equals(session.getOpenerUserId());
        
        return new SessionInfo(
                session.getId(),
                session.getLotteryId(),
                session.getOpenerUserId(),
                isOpener,
                session.getProtectionDraws() != null ? session.getProtectionDraws() : 0,
                session.getProtectionEndTime(),
                session.getOpenerDrawCount() != null ? session.getOpenerDrawCount() : 0,
                session.getFreeDrawEnabled() != null && session.getFreeDrawEnabled() == 1,
                session.getStatus(),
                session.getPlayerDesignatedNumbers(),
                session.getDesignationDeadline()  // 🆕
        );
    }

    @Override
    public List<DesignatedWinningNumber> getDesignatedWinningNumbers(String lotteryId) {
        log.info("🏆 查詢已指定大獎中獎號碼: lotteryId={}", lotteryId);
        
        // 查詢所有 isDesignatedPrize=1 的籤位
        LotteryTicketExample ticketEx = new LotteryTicketExample();
        ticketEx.createCriteria()
                .andLotteryIdEqualTo(lotteryId)
                .andIsDesignatedPrizeEqualTo((byte) 1);
        ticketEx.setOrderByClause("revealed_number ASC");
        List<LotteryTicket> designatedTickets = lotteryTicketMapper.selectByExample(ticketEx);
        
        if (designatedTickets.isEmpty()) {
            log.info("ℹ️ 尚未有指定的大獎號碼");
            return List.of();
        }
        
        List<DesignatedWinningNumber> result = new ArrayList<>();
        for (LotteryTicket ticket : designatedTickets) {
            String prizeId = ticket.getPrizeId();
            String prizeName = null;
            String prizeLevel = ticket.getPrizeLevel();
            String prizeImageUrl = null;
            
            if (prizeId != null) {
                LotteryPrize prize = lotteryPrizeMapper.selectByPrimaryKey(prizeId);
                if (prize != null) {
                    prizeName = prize.getName();
                    prizeLevel = prize.getLevel();
                    prizeImageUrl = prize.getImageUrl();
                }
            }
            
            result.add(new DesignatedWinningNumber(
                    ticket.getRevealedNumber(),
                    prizeId,
                    prizeName,
                    prizeLevel,
                    prizeImageUrl
            ));
        }
        
        log.info("✅ 已指定大獎號碼: {} 個", result.size());
        return result;
    }

    /**
     * 啟動保護時間（首次抽獎時呼叫）
     * <p>將 session 的 protectionStartTime/protectionEndTime 設定為當前時間 + protectionMinutes</p>
     */
    @Transactional
    public void startProtection(String sessionId, String lotteryId) {
        LotterySession session = lotterySessionMapper.selectByPrimaryKey(sessionId);
        if (session == null || session.getProtectionEndTime() != null) {
            return; // 已啟動或不存在
        }
        
        Integer protectionMinutes = systemConfigService.getInt(
            SystemConfigService.KEY_PROTECTION_INITIAL_MINUTES,
            5
        );
        
        LocalDateTime now = LocalDateTime.now();
        session.setProtectionStartTime(now);
        session.setProtectionEndTime(now.plusMinutes(protectionMinutes));
        session.setUpdatedAt(now);
        lotterySessionMapper.updateByPrimaryKey(session);
        
        log.info("🛡️ 保護時間啟動: sessionId={}, protectionEnd={}", sessionId, session.getProtectionEndTime());
    }

    /**
     * 延長保護時間（每次 API 操作延長一次，最多到上限）
     */
    @Transactional
    public void extendProtection(String sessionId) {
        LotterySession session = lotterySessionMapper.selectByPrimaryKey(sessionId);
        if (session == null || session.getProtectionStartTime() == null || session.getProtectionEndTime() == null) {
            return;
        }

        int extensionMinutes = systemConfigService.getInt(
                SystemConfigService.KEY_PROTECTION_EXTENSION_MINUTES,
                2
        );
        int maxMinutes = systemConfigService.getInt(
                SystemConfigService.KEY_PROTECTION_MAX_MINUTES,
                10
        );

        LocalDateTime maxEndTime = session.getProtectionStartTime().plusMinutes(maxMinutes);
        LocalDateTime candidateEndTime = session.getProtectionEndTime().plusMinutes(extensionMinutes);
        LocalDateTime newEndTime = candidateEndTime.isAfter(maxEndTime) ? maxEndTime : candidateEndTime;

        if (newEndTime.isAfter(session.getProtectionEndTime())) {
            session.setProtectionEndTime(newEndTime);
            session.setUpdatedAt(LocalDateTime.now());
            lotterySessionMapper.updateByPrimaryKey(session);
            log.info("🛡️ 保護時間延長: sessionId={}, newEndTime={}", sessionId, newEndTime);
        }
    }

    /**
     * 取得扭蛋鎖物件（Controller 用於 synchronized）
     */
    public Object getGachaLock(String lotteryId) {
        return gachaLocks.computeIfAbsent(lotteryId, k -> new Object());
    }

    @Override
    public DesignationCheckResponse getDesignationStatus(String lotteryId, String userId) {
        log.info("🔍 查詢指定狀態: lotteryId={}, userId={}", lotteryId, userId);

        Lottery lottery = lotteryMapper.selectByPrimaryKey(lotteryId);
        if (lottery == null) {
            return DesignationCheckResponse.builder().required(false).build();
        }

        // 非 SCRATCH_PLAYER 模式 → 不需要指定
        if (!GameModeEnum.SCRATCH_PLAYER.getCode().equals(lottery.getGameMode())) {
            return DesignationCheckResponse.builder()
                    .required(false)
                    .gameMode(lottery.getGameMode())
                    .build();
        }

        // 查詢 ACTIVE session
        LotterySessionExample example = new LotterySessionExample();
        example.createCriteria()
                .andLotteryIdEqualTo(lotteryId)
                .andStatusEqualTo("ACTIVE");
        List<LotterySession> sessions = lotterySessionMapper.selectByExample(example);

        if (sessions.isEmpty()) {
            return DesignationCheckResponse.builder()
                    .required(false)
                    .gameMode(lottery.getGameMode())
                    .build();
        }

        LotterySession session = sessions.get(0);
        boolean isOpener = userId != null && userId.equals(session.getOpenerUserId());

        // 已完成指定
        String designated = session.getPlayerDesignatedNumbers();
        if (designated != null && !designated.isBlank()) {
            return DesignationCheckResponse.builder()
                    .required(false)
                    .gameMode(lottery.getGameMode())
                    .sessionId(session.getId())
                    .isOpener(isOpener)
                    .alreadyDesignated(true)
                    .build();
        }

        // 需要指定
        if (isOpener) {
            // 查詢大獎獎品
            LotteryPrizeExample prizeEx = new LotteryPrizeExample();
            prizeEx.createCriteria()
                    .andLotteryIdEqualTo(lotteryId)
                    .andIsGrandPrizeEqualTo((byte) 1);
            List<com.group.admin.entity.LotteryPrize> grandPrizes = lotteryPrizeMapper.selectByExample(prizeEx);

            List<DesignationCheckResponse.GrandPrize> grandPrizeList = grandPrizes.stream()
                    .map(p -> DesignationCheckResponse.GrandPrize.builder()
                            .prizeId(p.getId())
                            .prizeName(p.getName())
                            .prizeLevel(p.getLevel())
                            .prizeImageUrl(p.getImageUrl())
                            .build())
                    .collect(Collectors.toList());

            // 查詢可用 revealedNumbers
            List<Integer> availableNumbers = getAvailableRevealedNumbers(lotteryId);

            int requiredCount = grandPrizes.stream()
                    .mapToInt(p -> p.getQuantity() != null ? p.getQuantity() : 0)
                    .sum();

            return DesignationCheckResponse.builder()
                    .required(true)
                    .gameMode(lottery.getGameMode())
                    .sessionId(session.getId())
                    .isOpener(true)
                    .requiredDesignationCount(requiredCount)
                    .grandPrizes(grandPrizeList)
                    .availableRevealedNumbers(availableNumbers)
                    .build();
        } else {
            // 非開套玩家 → 等待開套玩家指定
            return DesignationCheckResponse.builder()
                    .required(true)
                    .gameMode(lottery.getGameMode())
                    .sessionId(session.getId())
                    .isOpener(false)
                    .message("等待開套玩家指定大獎位置")
                    .build();
        }
    }
}
