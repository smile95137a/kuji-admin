package com.group.admin.service.impl;

import com.group.admin.entity.Lottery;
import com.group.admin.entity.LotteryPrize;
import com.group.admin.exception.BusinessException;
import com.group.admin.mapper.LotteryMapper;
import com.group.admin.mapper.LotteryPrizeMapper;
import com.group.admin.res.lottery.LotteryTicketRes;
import com.group.admin.service.LotteryTicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    // TODO: 需要新增以下 Mapper（執行 MBG 後）
    // private final LotteryTicketMapper ticketMapper;
    // private final LotterySessionMapper sessionMapper;
    // private final LotteryDrawRecordMapper drawRecordMapper;
    // private final UserMapper userMapper;
    // private final PointLogMapper pointLogMapper;

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

        String gameMode = lottery.getSubCategory(); // 暫用 subCategory，待新增 gameMode 欄位
        int totalTickets = lottery.getMaxDraws();
        
        if (totalTickets <= 0) {
            throw new BusinessException("總抽數必須大於 0");
        }

        // 根據遊戲模式生成籤位
        switch (gameMode != null ? gameMode : "LOTTERY_MODE") {
            case "LOTTERY_MODE" -> generateRandomTickets(lotteryId, totalTickets);
            case "SCRATCH_CARD_MODE" -> generateScratchTickets(lotteryId, totalTickets, lottery);
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
        // TODO: 使用 LotteryPrizeExample 查詢
        // List<LotteryPrize> prizes = prizeMapper.selectByLotteryId(lotteryId);
        
        // 模擬獎品資料（實際需從資料庫取得）
        List<PrizeSlot> prizePool = new ArrayList<>();
        
        // TODO: 實際實作
        // for (LotteryPrize prize : prizes) {
        //     for (int i = 0; i < prize.getQuantity(); i++) {
        //         prizePool.add(new PrizeSlot(prize.getId(), prize.getLevel()));
        //     }
        // }
        
        // 如果獎品總數 < 總籤位數，補上「謝謝惠顧」
        while (prizePool.size() < totalTickets) {
            prizePool.add(new PrizeSlot(null, "THANKS"));
        }
        
        // 打亂順序（核心：隨機分配）
        Collections.shuffle(prizePool, random);
        
        // 生成籤位資料
        // TODO: 批量插入 lottery_ticket
        // for (int i = 0; i < totalTickets; i++) {
        //     LotteryTicket ticket = new LotteryTicket();
        //     ticket.setId(UUID.randomUUID().toString());
        //     ticket.setLotteryId(lotteryId);
        //     ticket.setTicketNumber(i + 1);
        //     ticket.setPrizeId(prizePool.get(i).prizeId());
        //     ticket.setPrizeLevel(prizePool.get(i).level());
        //     ticket.setStatus("AVAILABLE");
        //     ticketMapper.insert(ticket);
        // }
        
        log.info("✅ 隨機籤位生成完成，獎品分配範例: 1號={}, 2號={}, ...{}號={}", 
                prizePool.get(0).level(), 
                prizePool.get(1).level(),
                totalTickets,
                prizePool.get(totalTickets - 1).level());
    }

    /**
     * 生成刮刮樂籤位
     * 
     * <p>店家指定模式：指定位置為大獎，其餘為謝謝惠顧</p>
     * <p>玩家指定模式：先生成全部「謝謝惠顧」，等開套玩家指定</p>
     */
    private void generateScratchTickets(String lotteryId, int totalTickets, Lottery lottery) {
        log.info("🎫 生成刮刮樂籤位: lotteryId={}, total={}", lotteryId, totalTickets);

        // 解析店家指定的大獎位置（JSON Array）
        Set<Integer> designatedNumbers = new HashSet<>();
        // TODO: 從 lottery.getDesignatedPrizeNumbers() 解析
        // String json = lottery.getDesignatedPrizeNumbers();
        // if (json != null && !json.isEmpty()) {
        //     designatedNumbers = parseJsonArray(json);
        // }

        // 取得大獎獎品 ID
        String grandPrizeId = null;
        // TODO: 查詢 is_grand_prize = 1 的獎品
        // LotteryPrize grandPrize = prizeMapper.findGrandPrize(lotteryId);
        // if (grandPrize != null) grandPrizeId = grandPrize.getId();

        // 生成籤位
        for (int i = 1; i <= totalTickets; i++) {
            // TODO: 插入籤位
            // LotteryTicket ticket = new LotteryTicket();
            // ticket.setId(UUID.randomUUID().toString());
            // ticket.setLotteryId(lotteryId);
            // ticket.setTicketNumber(i);
            // 
            // if (designatedNumbers.contains(i)) {
            //     // 店家指定的大獎位置
            //     ticket.setPrizeId(grandPrizeId);
            //     ticket.setPrizeLevel("GRAND");
            //     ticket.setIsDesignatedPrize((byte) 1);
            //     ticket.setDesignatedBy("STORE");
            // } else {
            //     // 謝謝惠顧
            //     ticket.setPrizeId(null);
            //     ticket.setPrizeLevel("THANKS");
            // }
            // ticket.setStatus("AVAILABLE");
            // ticketMapper.insert(ticket);
        }
        
        log.info("✅ 刮刮樂籤位生成完成，大獎位置: {}", designatedNumbers);
    }

    @Override
    @Transactional
    public void designatePrizePositions(String lotteryId, String userId, List<Integer> prizeNumbers) {
        log.info("🎯 開套玩家指定大獎位置: lotteryId={}, userId={}, numbers={}", 
                lotteryId, userId, prizeNumbers);
        
        // TODO: 驗證是否為開套玩家
        // TODO: 驗證商品是否為「玩家指定」模式
        // TODO: 更新指定籤位的 prize_id, prize_level, is_designated_prize, designated_by
        
        throw new UnsupportedOperationException("待實作：需要 LotteryTicketMapper");
    }

    // ==================== 前台籤位查詢 ====================

    @Override
    public List<LotteryTicketRes> getTicketsForFrontend(String lotteryId) {
        log.info("🔍 前台查詢籤位: lotteryId={}", lotteryId);
        
        // TODO: 查詢所有籤位
        // List<LotteryTicket> tickets = ticketMapper.selectByLotteryId(lotteryId);
        
        // 轉換並過濾敏感資訊
        // return tickets.stream()
        //     .map(this::toRes)
        //     .map(LotteryTicketRes::forFrontend)  // ⚠️ 關鍵：隱藏未抽籤位的獎品資訊
        //     .collect(Collectors.toList());
        
        return Collections.emptyList(); // TODO: 實作
    }

    @Override
    public List<LotteryTicketRes> getTicketsForBackend(String lotteryId) {
        log.info("🔍 後台查詢籤位: lotteryId={}", lotteryId);
        
        // TODO: 查詢所有籤位（完整資訊）
        // List<LotteryTicket> tickets = ticketMapper.selectByLotteryId(lotteryId);
        // return tickets.stream().map(this::toRes).collect(Collectors.toList());
        
        return Collections.emptyList(); // TODO: 實作
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
        
        // 2. 取得或建立場次
        SessionInfo session = getOrCreateSession(lotteryId, userId);
        
        // 3. 檢查保護時間
        if (!canDrawNow(lotteryId, userId)) {
            return new DrawResult(false, null, 0, null, null, null, null, false, false, 0L, 
                    "商品正在被其他玩家抽獎中，請稍後再試");
        }
        
        // 4. 檢查餘額
        // TODO: 從 UserMapper 查詢使用者餘額
        Long pricePerDraw = lottery.getPricePerDraw();
        // User user = userMapper.selectByPrimaryKey(userId);
        // if (user.getGoldCoins() < pricePerDraw) {
        //     return new DrawResult(false, ..., "餘額不足");
        // }
        
        // 5. 決定籤位
        int actualTicketNumber;
        if (ticketNumber != null) {
            // 選號模式
            actualTicketNumber = ticketNumber;
            // TODO: 檢查該籤位是否可用
        } else {
            // 隨機模式
            Integer randomTicket = getRandomAvailableTicket(lotteryId);
            if (randomTicket == null) {
                return new DrawResult(false, null, 0, null, null, null, null, false, false, 0L, "已無可抽籤位");
            }
            actualTicketNumber = randomTicket;
        }
        
        // 6-9. TODO: 更新籤位、扣款、檢查免單、記錄
        
        // 10. 返回結果（模擬）
        return new DrawResult(
                true, 
                UUID.randomUUID().toString(), 
                actualTicketNumber, 
                null, 
                "C", 
                "模擬獎品", 
                null, 
                false, 
                false, 
                0L, 
                "抽獎成功"
        );
    }

    @Override
    public Integer getRandomAvailableTicket(String lotteryId) {
        // TODO: 查詢所有 AVAILABLE 狀態的籤位，隨機選一個
        // List<Integer> availableNumbers = ticketMapper.selectAvailableNumbers(lotteryId);
        // if (availableNumbers.isEmpty()) return null;
        // return availableNumbers.get(random.nextInt(availableNumbers.size()));
        
        return 1; // TODO: 實作
    }

    // ==================== 開套場次管理 ====================

    @Override
    public SessionInfo getOrCreateSession(String lotteryId, String userId) {
        // TODO: 查詢是否有進行中的場次
        // LotterySession activeSession = sessionMapper.findActiveSession(lotteryId);
        // 
        // if (activeSession != null) {
        //     // 有進行中的場次
        //     boolean isOpener = activeSession.getOpenerUserId().equals(userId);
        //     return new SessionInfo(...);
        // }
        //
        // // 建立新場次（當前使用者成為開套者）
        // Lottery lottery = lotteryMapper.selectByPrimaryKey(lotteryId);
        // LotterySession newSession = new LotterySession();
        // newSession.setId(UUID.randomUUID().toString());
        // newSession.setLotteryId(lotteryId);
        // newSession.setOpenerUserId(userId);
        // newSession.setProtectionDraws(lottery.getProtectionDraws());
        // newSession.setProtectionStartTime(LocalDateTime.now());
        // newSession.setProtectionEndTime(LocalDateTime.now().plusMinutes(lottery.getProtectionMinutes()));
        // newSession.setFreeDrawEnabled(lottery.getFreeDrawEnabled());
        // newSession.setStatus("ACTIVE");
        // sessionMapper.insert(newSession);
        
        return new SessionInfo(
                UUID.randomUUID().toString(),
                lotteryId,
                userId,
                true,
                5,
                LocalDateTime.now().plusMinutes(5),
                0,
                true,
                "ACTIVE"
        );
    }

    @Override
    public boolean canDrawNow(String lotteryId, String userId) {
        // TODO: 檢查是否有其他玩家的進行中場次
        // LotterySession activeSession = sessionMapper.findActiveSession(lotteryId);
        // if (activeSession == null) return true;
        // if (activeSession.getOpenerUserId().equals(userId)) return true;
        // if (activeSession.getProtectionEndTime().isBefore(LocalDateTime.now())) {
        //     // 保護時間已過，設為過期
        //     activeSession.setStatus("EXPIRED");
        //     sessionMapper.updateByPrimaryKey(activeSession);
        //     return true;
        // }
        // return false;
        
        return true; // TODO: 實作
    }

    @Override
    public void expireOldSessions() {
        log.info("⏰ 清理過期場次...");
        // TODO: 定時任務呼叫
        // sessionMapper.expireByTime(LocalDateTime.now());
    }

    // ==================== 免單機制 ====================

    @Override
    public boolean checkAndTriggerFreeDraw(String sessionId, String prizeId) {
        // TODO: 實作免單檢查
        // LotterySession session = sessionMapper.selectByPrimaryKey(sessionId);
        // if (session == null) return false;
        // if (session.getFreeDrawEnabled() != 1) return false;
        // if (session.getOpenerDrawCount() > session.getProtectionDraws()) return false;
        //
        // // 檢查是否中大獎
        // LotteryPrize prize = prizeMapper.selectByPrimaryKey(prizeId);
        // if (prize == null || prize.getIsGrandPrize() != 1) return false;
        //
        // // 觸發免單：退款
        // Long refundAmount = session.getOpenerTotalCost();
        // session.setFreeDrawTriggered((byte) 1);
        // session.setFreeDrawRefundAmount(refundAmount);
        // session.setFreeDrawTriggeredAt(LocalDateTime.now());
        // session.setFreeDrawPrizeId(prizeId);
        // sessionMapper.updateByPrimaryKey(session);
        //
        // // 退款給使用者
        // userService.addGoldCoins(session.getOpenerUserId(), refundAmount, "開套免單退款");
        // return true;
        
        return false; // TODO: 實作
    }

    // ==================== 內部輔助 ====================

    private record PrizeSlot(String prizeId, String level) {}
}
