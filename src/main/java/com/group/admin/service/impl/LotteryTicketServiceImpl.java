package com.group.admin.service.impl;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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
import com.group.admin.example.LotteryExample;
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
 * 抽獎籤位與指定邏輯實作
 *
 * <p>此服務負責生成籤位、處理刮刮樂的指定大獎、以及相關票務檢查。</p>
 * <ul>
 *   <li>生成隨機籤位（LOTTERY_MODE）</li>
 *   <li>生成刮刮樂籤位（SCRATCH_MODE / SCRATCH_CARD_MODE）</li>
 *   <li>提供前台/後台大獎指定 API 支援</li>
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

    @Lazy
    @Autowired
    private LotteryService lotteryService;

    private final SecureRandom random = new SecureRandom();
    private final Map<String, Object> gachaLocks = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public void generateTickets(String lotteryId) {
        Lottery lottery = lotteryMapper.selectByPrimaryKey(lotteryId);
        if (lottery == null) {
            throw new BusinessException("找不到抽獎活動: " + lotteryId);
        }

        String gameMode = lottery.getPlayMode();
        int totalTickets = lottery.getMaxDraws();

        if (totalTickets <= 0) {
            throw new BusinessException("maxDraws 必須大於 0");
        }

        log.info("生成籤位模式: {}, 總籤數: {}", gameMode, totalTickets);

        switch (gameMode != null ? gameMode : "LOTTERY_MODE") {
            case "LOTTERY_MODE" -> generateRandomTickets(lotteryId, totalTickets);
            case "SCRATCH_MODE", "SCRATCH_CARD_MODE" -> generateScratchTickets(lotteryId, totalTickets, lottery);
            default -> generateRandomTickets(lotteryId, totalTickets);
        }

        log.info("生成籤位完成: lotteryId={}, totalTickets={}", lotteryId, totalTickets);
    }

    /**
     * 生成隨機籤位（LOTTERY_MODE）
     *
     * <p>流程說明：</p>
     * <ol>
     *   <li>讀取該抽獎的獎項與數量。</li>
     *   <li>根據每個獎項建立對應數量的獎品槽（PrizeSlot）。</li>
     *   <li>確認獎品總數等於籤位總數，打亂後依序建立 `LotteryTicket` (ticketNumber 1..N)。</li>
     *   <li>每張票設定狀態、獎項、建立/更新時間。</li>
     * </ol>
     */
    private void generateRandomTickets(String lotteryId, int totalTickets) {
        log.info("[生成隨機籤位] lotteryId={}, total={}", lotteryId, totalTickets);

        // 讀取該抽獎的獎項清單
        LotteryPrizeExample prizeExample = new LotteryPrizeExample();
        prizeExample.createCriteria().andLotteryIdEqualTo(lotteryId);
        prizeExample.setOrderByClause("order_num ASC");
        List<LotteryPrize> prizes = lotteryPrizeMapper.selectByExample(prizeExample);
        if (prizes.isEmpty()) {
            log.error("[錯誤] 抽獎未設定獎項: lotteryId={}", lotteryId);
            throw new BusinessException("該抽獎尚未設定獎項: " + lotteryId);
        }

        // 建立獎池（每個獎項依數量 expand 成多個 PrizeSlot）
        List<PrizeSlot> prizePool = new ArrayList<>();
        for (LotteryPrize prize : prizes) {
            if (prize.getIsLastPrize() != null && prize.getIsLastPrize() == 1) {
                log.info("跳過末獎配置: {} (quantity={})", prize.getName(), prize.getQuantity());
                continue;
            }
            int quantity = prize.getQuantity() != null ? prize.getQuantity() : 0;
            for (int i = 0; i < quantity; i++) {
                prizePool.add(new PrizeSlot(prize.getId(), prize.getLevel()));
            }
        }
        log.info("[獎池大小] prizePoolSize={}, totalTickets={}", prizePool.size(), totalTickets);

        // 確認獎池大小與籤位數一致，否則視為設定錯誤
        if (prizePool.size() != totalTickets) {
            log.error("獎池大小與總籤數不符: prizePool={}, totalTickets={}", prizePool.size(), totalTickets);
            throw new BusinessException(String.format("獎項總數(%d)與籤位總數(%d)不相符", prizePool.size(), totalTickets));
        }

        // 隨機打散獎池
        Collections.shuffle(prizePool, random);
        
        // 建立並儲存每張票，設定票號、獎項與狀態
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
        
        // 簡短列印幾個範例等級供除錯用
        if (prizePool.size() >= 1) {
            String firstLevel = prizePool.get(0).level();
            String lastLevel = prizePool.get(prizePool.size() - 1).level();
            log.debug("獎池範例等級: first={}, last={}, total={}", firstLevel, lastLevel, prizePool.size());
        }
    }

    /**
     * 生成刮刮樂籤位（SCRATCH_* 模式）
     *
     * <p>說明：</p>
     * <ul>
     *   <li>`ticketNumber`：對外顯示的票序號 (1..N)</li>
     *   <li>`revealedNumber`：刮開後顯示的亂數（1..N），會被打亂並綁定至票位</li>
     *   <li>若為 SCRATCH_STORE，後台可指定 `designatedPrizeNumbers` 對應的 revealedNumber 為大獎</li>
     *   <li>若為 SCRATCH_PLAYER，玩家需透過 /designate API 指定 revealedNumber 作為大獎</li>
     * </ul>
     */
    private void generateScratchTickets(String lotteryId, int totalTickets, Lottery lottery) {
        log.info("[生成刮刮樂籤位] lotteryId={}, total={}", lotteryId, totalTickets);

        // 步驟1：準備 1..N 的 revealedNumber 並打亂
        List<Integer> revealedNumbers = new ArrayList<>();
        for (int i = 1; i <= totalTickets; i++) revealedNumbers.add(i);
        Collections.shuffle(revealedNumbers, random);

        // 步驟2：解析後台指定的 revealedNumber（若為 SCRATCH_STORE），或保留讓玩家指定（SCRATCH_PLAYER）
        Set<Integer> winningRevealedNumbers = new HashSet<>();
        String gameMode = lottery.getGameMode(); // SCRATCH_STORE / SCRATCH_PLAYER / RANDOM
        if (GameModeEnum.SCRATCH_STORE.getCode().equals(gameMode)) {
            winningRevealedNumbers.addAll(parseDesignatedPrizeNumbers(lottery.getDesignatedPrizeNumbers()));
        }

        // 步驟3：建立獎池並將部分獎項（若有）指派給指定的 revealedNumber
        LotteryPrizeExample prizeExample = new LotteryPrizeExample();
        prizeExample.createCriteria().andLotteryIdEqualTo(lotteryId);
        prizeExample.setOrderByClause("order_num ASC");
        List<LotteryPrize> prizes = lotteryPrizeMapper.selectByExample(prizeExample);
        List<LotteryPrize> grandPrizes = prizes.stream()
                .filter(prize -> prize.getIsGrandPrize() != null && prize.getIsGrandPrize() == 1)
                .toList();

        if (GameModeEnum.SCRATCH_STORE.getCode().equals(gameMode)) {
            validateScratchStoreDesignation(winningRevealedNumbers, grandPrizes, totalTickets);
        }

        List<PrizeSlot> prizePool = new ArrayList<>();
        for (LotteryPrize prize : prizes) {
            int quantity = prize.getQuantity() != null ? prize.getQuantity() : 0;
            for (int i = 0; i < quantity; i++) {
                prizePool.add(new PrizeSlot(prize.getId(), prize.getLevel()));
            }
        }

        // 若後台有指定大獎 revealedNumber，將對應的獎品分配到該 revealedNumber
        Map<Integer, PrizeSlot> revealedToPrize = new HashMap<>();
        List<PrizeSlot> grandPrizeSlots = new ArrayList<>();
        for (LotteryPrize prize : grandPrizes) {
            int quantity = prize.getQuantity() != null ? prize.getQuantity() : 0;
            for (int i = 0; i < quantity; i++) {
                grandPrizeSlots.add(new PrizeSlot(prize.getId(), prize.getLevel()));
            }
        }
        Iterator<PrizeSlot> grandPrizeIterator = grandPrizeSlots.iterator();
        for (Integer winNum : winningRevealedNumbers.stream().sorted().toList()) {
            if (!grandPrizeIterator.hasNext()) {
                throw new BusinessException("SCRATCH_STORE 指定大獎號碼數量超過大獎數量");
            }
            revealedToPrize.put(winNum, grandPrizeIterator.next());
        }

        // 步驟4：建立每張票並註記是否為指定大獎
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < totalTickets; i++) {
            int revealedNumber = revealedNumbers.get(i);
            PrizeSlot slot = revealedToPrize.get(revealedNumber);

            LotteryTicket ticket = new LotteryTicket();
            ticket.setId(UUID.randomUUID().toString());
            ticket.setLotteryId(lotteryId);
            ticket.setTicketNumber(i + 1);              // 對外票序號
            ticket.setRevealedNumber(revealedNumber);   // 真正的 revealedNumber（玩家刮開顯示）
            ticket.setPrizeId(slot != null ? slot.prizeId() : null);
            ticket.setPrizeLevel(slot != null ? slot.level() : "THANKS");
            ticket.setStatus("AVAILABLE");
            ticket.setIsDesignatedPrize(slot != null ? (byte) 1 : (byte) 0);
            ticket.setDesignatedBy(slot != null ? "STORE" : null);
            ticket.setCreatedAt(now);
            ticket.setUpdatedAt(now);
            lotteryTicketMapper.insert(ticket);
        }

        log.info("[刮刮樂獎項綁定] lotteryId={}, winningRevealedNumbers={}, nonGrandCount={}",
            lotteryId, winningRevealedNumbers, totalTickets - winningRevealedNumbers.size());

        // 若為 SCRATCH_STORE，完成指定後自動分配其餘非大獎獎項
        if (GameModeEnum.SCRATCH_STORE.getCode().equals(gameMode) && !winningRevealedNumbers.isEmpty()) {
            autoAssignNonGrandPrizes(lotteryId);
        }
    }

    private List<Integer> parseDesignatedPrizeNumbers(String designatedPrizeNumbers) {
        if (designatedPrizeNumbers == null || designatedPrizeNumbers.trim().isEmpty()) {
            return List.of();
        }

        String cleaned = designatedPrizeNumbers.trim().replaceAll("[\\[\\]\\s]", "");
        if (cleaned.isEmpty()) {
            return List.of();
        }

        List<Integer> result = new ArrayList<>();
        for (String numStr : cleaned.split(",")) {
            if (numStr == null || numStr.isBlank()) {
                continue;
            }
            try {
                result.add(Integer.parseInt(numStr.trim()));
            } catch (NumberFormatException e) {
                throw new BusinessException("designatedPrizeNumbers 格式錯誤，請使用數字清單，例如 [5] 或 5,12");
            }
        }
        return result;
    }

    private void validateScratchStoreDesignation(Set<Integer> winningRevealedNumbers,
            List<LotteryPrize> grandPrizes,
            int totalTickets) {
        if (winningRevealedNumbers.isEmpty()) {
            throw new BusinessException("SCRATCH_STORE 模式必須指定 designatedPrizeNumbers");
        }

        int requiredGrandPrizeCount = grandPrizes.stream()
                .mapToInt(prize -> prize.getQuantity() != null ? prize.getQuantity() : 0)
                .sum();

        if (requiredGrandPrizeCount <= 0) {
            throw new BusinessException("SCRATCH_STORE 模式缺少大獎設定，無法生成指定籤位");
        }

        if (winningRevealedNumbers.size() != requiredGrandPrizeCount) {
            throw new BusinessException(String.format(
                    "SCRATCH_STORE 指定號碼數量(%d)必須等於大獎數量(%d)",
                    winningRevealedNumbers.size(),
                    requiredGrandPrizeCount));
        }

        for (Integer revealedNumber : winningRevealedNumbers) {
            if (revealedNumber == null || revealedNumber < 1 || revealedNumber > totalTickets) {
                throw new BusinessException(String.format(
                        "SCRATCH_STORE 指定號碼 %s 超出可用範圍 1~%d",
                        String.valueOf(revealedNumber),
                        totalTickets));
            }
        }
    }

    @Override
    @Transactional
    public void designatePrizePositions(String lotteryId, String userId, List<LotteryTicketService.PrizeDesignation> designations) {
        log.info("[玩家指定大獎] lotteryId={}, userId={}, designations={}", lotteryId, userId, designations);

        if (designations == null || designations.isEmpty()) {
            throw new BusinessException("指定清單不得為空");
        }

        requireScratchPlayerLottery(lotteryId);
        LotterySession session = requireActiveSession(lotteryId);
        syncPlayerDesignationFromTickets(lotteryId, session);

        if (!userId.equals(session.getOpenerUserId())) {
            throw new BusinessException("NOT_OPENER: only the opener can designate grand prize positions");
        }
        if (isDesignationCompleted(lotteryId, session)) {
            throw new BusinessException("ALREADY_DESIGNATED: 已完成指定，無法再次指定");
        }

        int requiredCount = getRequiredGrandPrizeCount(lotteryId);
        if (designations.size() != requiredCount) {
            throw new BusinessException("WRONG_DESIGNATION_COUNT: expected " + requiredCount + " designated grand prize slots");
        }

        Set<Integer> seenRevealedNumbers = new HashSet<>();
        LotteryTicketExample ticketExample = new LotteryTicketExample();
        ticketExample.createCriteria()
                .andLotteryIdEqualTo(lotteryId)
                .andStatusEqualTo("AVAILABLE");
        List<LotteryTicket> allTickets = lotteryTicketMapper.selectByExample(ticketExample);

        for (LotteryTicketService.PrizeDesignation designation : designations) {
            Integer revealedNumber = designation.revealedNumber();
            String prizeId = designation.prizeId();

            if (revealedNumber == null || prizeId == null) {
                throw new BusinessException("指定內容錯誤: revealedNumber 或 prizeId 為空");
            }
            if (!seenRevealedNumbers.add(revealedNumber)) {
                throw new BusinessException("DUPLICATE_REVEALED_NUMBER: 重複的 revealedNumber: " + revealedNumber);
            }

            LotteryPrize prize = lotteryPrizeMapper.selectByPrimaryKey(prizeId);
            if (prize == null) {
                throw new BusinessException("找不到獎項: " + prizeId);
            }
            if (!lotteryId.equals(prize.getLotteryId())) {
                throw new BusinessException("Prize does not belong to this lottery");
            }
            if (prize.getIsGrandPrize() == null || prize.getIsGrandPrize() != 1) {
                throw new BusinessException("指定的獎項不是大獎: " + prize.getName());
            }

            LotteryTicket target = allTickets.stream()
                    .filter(t -> revealedNumber.equals(t.getRevealedNumber()))
                    .findFirst()
                    .orElse(null);

            if (target == null) {
                throw new BusinessException("找不到對應的 revealedNumber: #" + revealedNumber);
            }
            if (target.getIsDesignatedPrize() != null
                    && target.getIsDesignatedPrize() == 1
                    && "PLAYER".equalsIgnoreCase(target.getDesignatedBy())) {
                throw new BusinessException("ALREADY_DESIGNATED: 該 revealedNumber 已由玩家指定: #" + revealedNumber);
            }

            target.setPrizeId(prizeId);
            target.setPrizeLevel(prize.getLevel());
            target.setIsDesignatedPrize((byte) 1);
            target.setDesignatedBy("PLAYER");
            target.setUpdatedAt(LocalDateTime.now());
            lotteryTicketMapper.updateByPrimaryKey(target);

            log.info("[指定] revealedNumber #{} -> {} ({})", revealedNumber, prize.getName(), prize.getLevel());
        }

        List<Integer> numbers = designations.stream()
                .map(LotteryTicketService.PrizeDesignation::revealedNumber)
                .toList();
        markPlayerDesignated(session.getId(), numbers);
        autoAssignNonGrandPrizes(lotteryId);

        log.info("[玩家指定完成] lotteryId={}, 指定數量={}", lotteryId, designations.size());
    }

    // ==================== 刪選 / 同步 / 查詢相關輔助方法 ====================

    @Override
    public List<LotteryTicketRes> getTicketsForFrontend(String lotteryId) {
        log.info("[取得供前端顯示的票表] lotteryId={}", lotteryId);
        
        LotteryTicketExample example = new LotteryTicketExample();
        example.createCriteria().andLotteryIdEqualTo(lotteryId);
        example.setOrderByClause("ticket_number ASC");
        List<LotteryTicket> tickets = lotteryTicketMapper.selectByExample(example);
        
        // 轉換並回傳供前端使用的票清單；未使用票會隱藏敏感獎項資訊
        List<LotteryTicketRes> result = new ArrayList<>();
        for (LotteryTicket ticket : tickets) {
            LotteryTicketRes res = toRes(ticket);
            // 對於尚未被使用(AVAILABLE)的票，隱藏獎項資訊以保密
            if ("AVAILABLE".equals(ticket.getStatus())) {
                res.setPrizeId(null);
                res.setPrizeName(null);
                res.setPrizeLevel(null);
                res.setPrizeImageUrl(null);           // 隱藏圖片 URL
                res.setIsGrandPrize(null);            // 隱藏是否為大獎
                res.setIsLastPrize(null);             // 隱藏是否為末獎
                res.setRevealedNumber(null);          // 隱藏 revealedNumber
            }
            result.add(res);
        }
        
        result.sort(java.util.Comparator.comparing(
                LotteryTicketRes::getTicketNumber,
                java.util.Comparator.nullsLast(Integer::compareTo)
        ));
        return result;
    }

    private void syncPlayerDesignationFromTickets(String lotteryId, LotterySession session) {
        if (session == null) {
            return;
        }

        Lottery lottery = lotteryMapper.selectByPrimaryKey(lotteryId);
        if (lottery == null || !GameModeEnum.SCRATCH_PLAYER.getCode().equals(lottery.getGameMode())) {
            return;
        }

        if (session.getPlayerDesignatedNumbers() != null && !session.getPlayerDesignatedNumbers().isBlank()) {
            return;
        }

        List<Integer> persistedNumbers = getPersistedPlayerDesignatedNumbers(lotteryId);
        if (persistedNumbers.isEmpty()) {
            return;
        }

        session.setPlayerDesignatedNumbers(persistedNumbers.toString());
        session.setDesignationDeadline(null);
        session.setUpdatedAt(LocalDateTime.now());
        lotterySessionMapper.updateByPrimaryKey(session);
    }

    private List<Integer> getPersistedPlayerDesignatedNumbers(String lotteryId) {
        LotteryTicketExample example = new LotteryTicketExample();
        example.createCriteria()
                .andLotteryIdEqualTo(lotteryId)
                .andIsDesignatedPrizeEqualTo((byte) 1)
                .andDesignatedByEqualTo("PLAYER");
        example.setOrderByClause("revealed_number ASC");

        return lotteryTicketMapper.selectByExample(example).stream()
                .map(LotteryTicket::getRevealedNumber)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .sorted()
                .toList();
    }


    private List<LotteryTicket> getPersistedPlayerDesignatedTickets(String lotteryId) {
        LotteryTicketExample example = new LotteryTicketExample();
        example.createCriteria()
                .andLotteryIdEqualTo(lotteryId)
                .andIsDesignatedPrizeEqualTo((byte) 1)
                .andDesignatedByEqualTo("PLAYER");
        example.setOrderByClause("updated_at ASC, created_at ASC, revealed_number ASC");
        return lotteryTicketMapper.selectByExample(example);
    }

    private boolean hasPersistedPlayerDesignations(String lotteryId) {
        return !getPersistedPlayerDesignatedNumbers(lotteryId).isEmpty();
    }

    private boolean isDesignationCompleted(String lotteryId, LotterySession session) {
        return (session != null
                && session.getPlayerDesignatedNumbers() != null
                && !session.getPlayerDesignatedNumbers().isBlank())
                || hasPersistedPlayerDesignations(lotteryId);
    }

    private Lottery requireScratchPlayerLottery(String lotteryId) {
        Lottery lottery = lotteryMapper.selectByPrimaryKey(lotteryId);
        if (lottery == null) {
            throw new BusinessException("找不到抽獎活動: " + lotteryId);
        }
        if (!GameModeEnum.SCRATCH_PLAYER.getCode().equals(lottery.getGameMode())) {
            throw new BusinessException("此抽獎非 SCRATCH_PLAYER 模式，無法執行玩家指定");
        }
        return lottery;
    }

    private LotterySession requireActiveSession(String lotteryId) {
        LotterySessionExample example = new LotterySessionExample();
        example.createCriteria()
                .andLotteryIdEqualTo(lotteryId)
                .andStatusEqualTo("ACTIVE");
        List<LotterySession> sessions = lotterySessionMapper.selectByExample(example);
        if (sessions.isEmpty()) {
            throw new BusinessException("No active scratch session found");
        }
        return sessions.get(0);
    }

    private int getRequiredGrandPrizeCount(String lotteryId) {
        LotteryPrizeExample prizeEx = new LotteryPrizeExample();
        prizeEx.createCriteria()
                .andLotteryIdEqualTo(lotteryId)
                .andIsGrandPrizeEqualTo((byte) 1);
        return lotteryPrizeMapper.selectByExample(prizeEx).stream()
                .mapToInt(p -> p.getQuantity() != null ? p.getQuantity() : 0)
                .sum();
    }

    @Transactional
    public int repairDuplicatePlayerDesignations() {
        LotteryExample lotteryExample = new LotteryExample();
        lotteryExample.createCriteria().andGameModeEqualTo(GameModeEnum.SCRATCH_PLAYER.getCode());
        List<Lottery> lotteries = lotteryMapper.selectByExample(lotteryExample);
        int repairedLotteries = 0;

        for (Lottery lottery : lotteries) {
            String lotteryId = lottery.getId();
            int requiredCount = getRequiredGrandPrizeCount(lotteryId);
            if (requiredCount <= 0) {
                continue;
            }

            List<LotteryTicket> designatedTickets = getPersistedPlayerDesignatedTickets(lotteryId);
            if (designatedTickets.size() <= requiredCount) {
                continue;
            }

            List<LotteryTicket> sortedTickets = designatedTickets.stream()
                    .sorted(Comparator
                            .comparing(LotteryTicket::getUpdatedAt, Comparator.nullsLast(LocalDateTime::compareTo))
                            .thenComparing(LotteryTicket::getCreatedAt, Comparator.nullsLast(LocalDateTime::compareTo))
                            .thenComparing(LotteryTicket::getRevealedNumber, Comparator.nullsLast(Integer::compareTo)))
                    .toList();

            List<LotteryTicket> preservedTickets = sortedTickets.subList(0, requiredCount);
            List<LotteryTicket> duplicateTickets = sortedTickets.subList(requiredCount, sortedTickets.size());

            for (LotteryTicket duplicateTicket : duplicateTickets) {
                duplicateTicket.setPrizeId(null);
                duplicateTicket.setPrizeLevel(null);
                duplicateTicket.setIsDesignatedPrize((byte) 0);
                duplicateTicket.setDesignatedBy(null);
                duplicateTicket.setUpdatedAt(LocalDateTime.now());
                lotteryTicketMapper.updateByPrimaryKey(duplicateTicket);
            }

            autoAssignNonGrandPrizes(lotteryId);

            List<Integer> preservedNumbers = preservedTickets.stream()
                    .map(LotteryTicket::getRevealedNumber)
                    .filter(java.util.Objects::nonNull)
                    .sorted()
                    .toList();

            LotterySessionExample sessionExample = new LotterySessionExample();
            sessionExample.createCriteria()
                    .andLotteryIdEqualTo(lotteryId)
                    .andStatusEqualTo("ACTIVE");
            List<LotterySession> sessions = lotterySessionMapper.selectByExample(sessionExample);
            for (LotterySession activeSession : sessions) {
                activeSession.setPlayerDesignatedNumbers(preservedNumbers.toString());
                activeSession.setDesignationDeadline(null);
                activeSession.setUpdatedAt(LocalDateTime.now());
                lotterySessionMapper.updateByPrimaryKey(activeSession);
            }

                repairedLotteries++;
                log.warn("[修復重複玩家指定] lotteryId={}, preservedNumbers={}, removedCount={}",
                    lotteryId, preservedNumbers, duplicateTickets.size());
        }

        return repairedLotteries;
    }    @Override
    public List<LotteryTicketRes> getTicketsForBackend(String lotteryId) {
        log.info("[取得後台票表] lotteryId={}", lotteryId);
        
        LotteryTicketExample example = new LotteryTicketExample();
        example.createCriteria().andLotteryIdEqualTo(lotteryId);
        example.setOrderByClause("ticket_number ASC");
        List<LotteryTicket> tickets = lotteryTicketMapper.selectByExample(example);
        
        // 後台直接回傳完整票清單（包含獎項資訊）
        List<LotteryTicketRes> result = new ArrayList<>();
        for (LotteryTicket ticket : tickets) {
            result.add(toRes(ticket));
        }
        
        return result;
    }
    
    /**
     * Entity -> Res 的轉換輔助
     */
    private LotteryTicketRes toRes(LotteryTicket ticket) {
        LotteryTicketRes res = new LotteryTicketRes();
        res.setId(ticket.getId());
        res.setTicketNumber(ticket.getTicketNumber());
        res.setRevealedNumber(ticket.getRevealedNumber());  // revealedNumber 可能為 null，對外顯示視需求隱藏
        res.setPrizeId(ticket.getPrizeId());
        res.setPrizeLevel(ticket.getPrizeLevel());
        res.setStatus(ticket.getStatus());
        res.setDrawnAt(ticket.getDrawnAt());
        res.setIsDesignatedPrize(ticket.getIsDesignatedPrize() != null && ticket.getIsDesignatedPrize() == 1);
        
        // 若 prizeId 不為空，查詢對應獎項並填充回傳物件的獎項資訊
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

    // ==================== 抽籤流程 (draw) =====================

    @Override
    @Transactional
    public DrawResult draw(String lotteryId, String userId, Integer ticketNumber, int drawCount) {
        log.info("[執行抽籤] lotteryId={}, userId={}, ticketNumber={}, count={}", 
            lotteryId, userId, ticketNumber, drawCount);
        
        // 1. 驗證抽獎活動存在與狀態（已上架）
        Lottery lottery = lotteryMapper.selectByPrimaryKey(lotteryId);
        if (lottery == null) {
            return new DrawResult(false, null, 0, null, null, null, null, null, false, false, 0L, "Lottery not found", false, null, null, null);
        }
        if (!"ON_SHELF".equals(lottery.getStatus())) {
            return new DrawResult(false, null, 0, null, null, null, null, null, false, false, 0L, "Lottery is not on shelf", false, null, null, null);
        }
        
        // 2. 若非 GACHA，檢查抽獎保護/冷卻 (canDrawNow)，避免短時間內重複抽取
        boolean isGacha = "GACHA".equals(lottery.getCategory());
        if (!isGacha && !canDrawNow(lotteryId, userId)) {
            return new DrawResult(false, null, 0, null, null, null, null, null, false, false, 0L, 
                    "Draw is blocked until protection ends", false, null, null, null);
        }
        
        // 3. 決定實際要抽的票號（使用指定票號或隨機可用票）
        int actualTicketNumber;
        LotteryTicket targetTicket;
        
        if (ticketNumber != null) {
            // 指定票號時，確認該票為 AVAILABLE
            LotteryTicketExample example = new LotteryTicketExample();
            example.createCriteria()
                    .andLotteryIdEqualTo(lotteryId)
                    .andTicketNumberEqualTo(ticketNumber)
                    .andStatusEqualTo("AVAILABLE");
            List<LotteryTicket> tickets = lotteryTicketMapper.selectByExample(example);
            
            if (tickets.isEmpty()) {
                return new DrawResult(false, null, 0, null, null, null, null, null, false, false, 0L, 
                        "Requested ticket is unavailable", false, null, null, null);
            }
            targetTicket = tickets.get(0);
            actualTicketNumber = ticketNumber;
        } else {
            // 以隨機方式取得一張可用票
            Integer randomNumber = getRandomAvailableTicket(lotteryId);
            if (randomNumber == null) {
                return new DrawResult(false, null, 0, null, null, null, null, null, false, false, 0L, "No available tickets", false, null, null, null);
            }
            
            // 依 ticketNumber 查詢票資料
            LotteryTicketExample example = new LotteryTicketExample();
            example.createCriteria()
                    .andLotteryIdEqualTo(lotteryId)
                    .andTicketNumberEqualTo(randomNumber);
            List<LotteryTicket> tickets = lotteryTicketMapper.selectByExample(example);
            
            if (tickets.isEmpty()) {
                return new DrawResult(false, null, 0, null, null, null, null, null, false, false, 0L, "Ticket not found", false, null, null, null);
            }
            targetTicket = tickets.get(0);
            actualTicketNumber = randomNumber;
        }
        
        // 4. 嘗試以樂觀更新方式將票標記為 DRAWN，避免競爭條件
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
                "Ticket was claimed by another user", false, null, null, null);
        }

        targetTicket.setStatus("DRAWN");
        targetTicket.setDrawnBy(userId);
        targetTicket.setDrawnAt(drawTime);
        targetTicket.setUpdatedAt(drawTime);
        
        log.info("[抽中結果] ticketNumber={}, prizeLevel={}", actualTicketNumber, targetTicket.getPrizeLevel());
        
        // 5. 處理獎項資訊（更新 remaining、準備回傳名稱/圖片/是否大獎）
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
                
                // 若獎項有 remaining 欄位，扣減庫存
                if (prize.getRemaining() != null && prize.getRemaining() > 0) {
                    prize.setRemaining(prize.getRemaining() - 1);
                    prize.setUpdatedAt(LocalDateTime.now());
                    lotteryPrizeMapper.updateByPrimaryKey(prize);
                }
            }
        } else {
            prizeName = "THANKS";
        }
        
        // 6. 更新抽獎次數 totalDraws
        if (lottery.getTotalDraws() == null) {
            lottery.setTotalDraws(1);
        } else {
            lottery.setTotalDraws(lottery.getTotalDraws() + 1);
        }
        lottery.setUpdatedAt(LocalDateTime.now());
        lotteryMapper.updateByPrimaryKey(lottery);
        
        // 7. 若中獎，將獎品加入 prize box（可能回收部分金額）
        if (prizeId != null) {
            try {
                // 計算回收金額（預設回收抽獎費用的一半）
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
                log.info("[發放獎品] userId={}, prizeId={}", userId, prizeId);
            } catch (Exception e) {
                log.error("發放獎品失敗: {}", e.getMessage());
                // 發放獎品時發生錯誤（已記錄），繼續後續流程
            }
        }
        
        // 8. 計算退款 / 觸發免費抽獎與付款處理
        Long pricePerDraw = lottery.getPricePerDraw() != null ? lottery.getPricePerDraw() : 0L;
        boolean triggeredFreeDraw = false;
        Long refundAmount = 0L;
        
        // 取得或建立玩家 Session（用於保護/延續策略）
        SessionInfo sessionInfo = getOrCreateSession(lotteryId, userId);
        
        // 檢查並啟動保護機制（protection）
        if (!isGacha && sessionInfo.protectionEndTime() == null) {
            startProtection(sessionInfo.sessionId(), lotteryId);
            log.info("[保護啟動] sessionId={}", sessionInfo.sessionId());
        } else if (!isGacha && sessionInfo.protectionEndTime() != null && sessionInfo.isOpener()) {
            extendProtection(sessionInfo.sessionId());
        }
        
        String paymentType = resolvePaymentType(lottery);

        // 根據 paymentType 進行扣款與記錄消費
        try {
            if ("BONUS".equals(paymentType)) {
                walletService.deductBonus(userId, pricePerDraw, "DRAW", lotteryId,
                    "抽獎 " + lottery.getTitle());
                consumptionRecordService.recordConsumption(
                    userId, "DRAW_BONUS", lotteryId, lottery.getTitle(),
                    null, null, 0L, pricePerDraw, "抽獎（使用紅利）");
            } else {
                walletService.deductGold(userId, pricePerDraw, "DRAW", lotteryId,
                    "抽獎 " + lottery.getTitle());
                consumptionRecordService.recordConsumption(
                    userId, "DRAW_GOLD", lotteryId, lottery.getTitle(),
                    null, null, pricePerDraw, 0L, "抽獎（使用現金）");
            }
            log.info("[付款] userId={}, amount={}, paymentType={}", userId, pricePerDraw, paymentType);
        } catch (BusinessException e) {
            log.error("付款失敗: {}", e.getMessage());
            throw e;
        }
        
        // 更新 Session（若為開啟者，更新統計資料）
        if (sessionInfo.isOpener()) {
            LotterySession session = lotterySessionMapper.selectByPrimaryKey(sessionInfo.sessionId());
            if (session != null) {
                session.setOpenerDrawCount((session.getOpenerDrawCount() != null ? session.getOpenerDrawCount() : 0) + 1);
                session.setOpenerTotalCost((session.getOpenerTotalCost() != null ? session.getOpenerTotalCost() : 0L) + pricePerDraw);
                session.setUpdatedAt(LocalDateTime.now());
                lotterySessionMapper.updateByPrimaryKey(session);
                
                // ?????????????
                if (prizeId != null && isGrandPrize) {
                    triggeredFreeDraw = checkAndTriggerFreeDraw(sessionInfo.sessionId(), prizeId);
                    if (triggeredFreeDraw) {
                        refundAmount = session.getOpenerTotalCost();
                    }
                }
            }
        }
        
        // 9. 檢查是否發放末獎並收集相關資訊
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

        // 10. 準備回傳 DrawResult 結果
        DrawResult result = new DrawResult(
                true, 
                targetTicket.getId(), 
                actualTicketNumber,
                targetTicket.getRevealedNumber(),  // revealedNumber 可能為 null；對外顯示視情況隱藏
                prizeId, 
                prizeLevel, 
                prizeName, 
                prizeImageUrl, 
                isGrandPrize, 
                triggeredFreeDraw,
                refundAmount,
                triggeredFreeDraw
                    ? ("已觸發免費抽獎，獎品：" + prizeName + "，退還金額：" + refundAmount)
                    : ("抽中獎品：" + prizeName),
                lastPrizeAwarded,
                lastPrizeId,
                lastPrizeName,
                lastPrizeImageUrl
        );

        // T016: 檢查並下架（如必要）
        lotteryService.checkAndDelist(lotteryId);

        return result;
    }

    @Override
    @Transactional
    public DrawResult drawByTicketId(String lotteryId, String userId, String ticketId) {
        log.info("[執行抽籤(依票ID)] lotteryId={}, userId={}, ticketId={}", lotteryId, userId, ticketId);

        // 1. 驗證抽獎活動存在與狀態
        Lottery lottery = lotteryMapper.selectByPrimaryKey(lotteryId);
        if (lottery == null) {
            return new DrawResult(false, ticketId, 0, null, null, null, null, null, false, false, 0L, "Lottery not found", false, null, null, null);
        }
        if (!"ON_SHELF".equals(lottery.getStatus())) {
            return new DrawResult(false, ticketId, 0, null, null, null, null, null, false, false, 0L, "Lottery is not on shelf", false, null, null, null);
        }

        // 2. 檢查抽獎保護/冷卻機制（避免短時間重複抽取）
        boolean isGacha = "GACHA".equals(lottery.getCategory());
        if (!isGacha && !canDrawNow(lotteryId, userId)) {
            return new DrawResult(false, ticketId, 0, null, null, null, null, null, false, false, 0L,
                    "Draw is blocked until protection ends", false, null, null, null);
        }

        // 3. 取得並驗證票據屬性
        LotteryTicket ticket = lotteryTicketMapper.selectByPrimaryKey(ticketId);
        if (ticket == null || ticket.getLotteryId() == null || !ticket.getLotteryId().equals(lotteryId)) {
            return new DrawResult(false, ticketId, 0, null, null, null, null, null, false, false, 0L, "該票不屬於此抽獎活動", false, null, null, null);
        }
        if (!"AVAILABLE".equals(ticket.getStatus())) {
            return new DrawResult(false, ticketId, ticket.getTicketNumber() != null ? ticket.getTicketNumber() : 0,
                    null, null, null, null, null, false, false, 0L, "Ticket is no longer available", false, null, null, null);
        }

        int actualTicketNumber = ticket.getTicketNumber() != null ? ticket.getTicketNumber() : 0;

        // 4. 標記票為已抽取（避免競爭條件）
        ticket.setStatus("DRAWN");
        ticket.setDrawnBy(userId);
        ticket.setDrawnAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());
        lotteryTicketMapper.updateByPrimaryKey(ticket);

        log.info("[抽中結果(ticketId)] ticketId={}, ticketNumber={}, prizeLevel={}", ticketId, actualTicketNumber, ticket.getPrizeLevel());

        // 5. 讀取獎項並處理 remaining
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

                // ?????????????
                if (prize.getRemaining() != null && prize.getRemaining() > 0) {
                    prize.setRemaining(prize.getRemaining() - 1);
                    prize.setUpdatedAt(LocalDateTime.now());
                    lotteryPrizeMapper.updateByPrimaryKey(prize);
                }
            }
        } else {
            prizeName = "THANKS";
        }

        // 6. 更新抽獎次數 totalDraws
        if (lottery.getTotalDraws() == null) {
            lottery.setTotalDraws(1);
        } else {
            lottery.setTotalDraws(lottery.getTotalDraws() + 1);
        }
        lottery.setUpdatedAt(LocalDateTime.now());
        lotteryMapper.updateByPrimaryKey(lottery);

        // 7. 若中獎，發放獎品到 prize box（並記錄回收金額）
        if (prizeId != null) {
                try {
                    Long recycleBonus = lottery.getPricePerDraw() != null ? lottery.getPricePerDraw() / 2 : 0L;
                    prizeBoxService.addToPrizeBox(userId, lotteryId, prizeId, lottery.getStoreId(), recycleBonus);
                    log.info("[發放獎品] userId={}, prizeId={}", userId, prizeId);
                } catch (Exception e) {
                    log.error("發放獎品失敗: {}", e.getMessage());
                }
        }

        // 8. 扣款與消費記錄
        Long pricePerDraw = lottery.getPricePerDraw() != null ? lottery.getPricePerDraw() : 0L;
        boolean triggeredFreeDraw = false;
        Long refundAmount = 0L;
        
        // 取得或建立玩家 Session
        SessionInfo sessionInfo = getOrCreateSession(lotteryId, userId);
        
        // 檢查並啟動或延長保護機制
        if (!isGacha && sessionInfo.protectionEndTime() == null) {
            startProtection(sessionInfo.sessionId(), lotteryId);
            log.info("[保護啟動] sessionId={}", sessionInfo.sessionId());
        } else if (!isGacha && sessionInfo.protectionEndTime() != null && sessionInfo.isOpener()) {
            extendProtection(sessionInfo.sessionId());
        }
        
        String paymentType = resolvePaymentType(lottery);

        // ??????? paymentType ?????????????????????????????????????????
        try {
                if ("BONUS".equals(paymentType)) {
                walletService.deductBonus(userId, pricePerDraw, "DRAW", lotteryId,
                    "抽獎 " + lottery.getTitle());
                consumptionRecordService.recordConsumption(
                    userId, "DRAW_BONUS", lotteryId, lottery.getTitle(),
                    null, null, 0L, pricePerDraw, "抽獎（使用紅利）");
                } else {
                walletService.deductGold(userId, pricePerDraw, "DRAW", lotteryId,
                    "抽獎 " + lottery.getTitle());
                consumptionRecordService.recordConsumption(
                    userId, "DRAW_GOLD", lotteryId, lottery.getTitle(),
                    null, null, pricePerDraw, 0L, "抽獎（使用現金）");
                }
                log.info("[付款] userId={}, amount={}, paymentType={}", userId, pricePerDraw, paymentType);
        } catch (BusinessException e) {
            log.error("付款失敗: {}", e.getMessage());
            throw e;
        }
        
        // ????? Session ?????????????????????????????????
        if (sessionInfo.isOpener()) {
            LotterySession session = lotterySessionMapper.selectByPrimaryKey(sessionInfo.sessionId());
            if (session != null) {
                session.setOpenerDrawCount((session.getOpenerDrawCount() != null ? session.getOpenerDrawCount() : 0) + 1);
                session.setOpenerTotalCost((session.getOpenerTotalCost() != null ? session.getOpenerTotalCost() : 0L) + pricePerDraw);
                session.setUpdatedAt(LocalDateTime.now());
                lotterySessionMapper.updateByPrimaryKey(session);
                
                // ?????????????
                if (prizeId != null && isGrandPrize) {
                    triggeredFreeDraw = checkAndTriggerFreeDraw(sessionInfo.sessionId(), prizeId);
                    if (triggeredFreeDraw) {
                        refundAmount = session.getOpenerTotalCost();
                    }
                }
            }
        }

        // ????????????????????????????????????????偃????????????????
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
                ticket.getRevealedNumber(),   // revealedNumber 可能為 null；對外顯示視情況隱藏
                prizeId,
                prizeLevel,
                prizeName,
                prizeImageUrl,
                isGrandPrize,
                triggeredFreeDraw,
                refundAmount,
                triggeredFreeDraw
                    ? ("已觸發免費抽獎，獎品：" + prizeName + "，退還金額：" + refundAmount)
                    : ("抽中獎品：" + prizeName),
                lastPrizeAwarded,
                lastPrizeId,
                lastPrizeName,
                lastPrizeImageUrl
        );

        // T016: 檢查並下架（如必要）
        lotteryService.checkAndDelist(lotteryId);

        return resultByTicketId;
    }

    @Override
    public Integer getRandomAvailableTicket(String lotteryId) {
        // ?????????AVAILABLE ??????????
        LotteryTicketExample example = new LotteryTicketExample();
        example.createCriteria()
                .andLotteryIdEqualTo(lotteryId)
                .andStatusEqualTo("AVAILABLE");
        List<LotteryTicket> availableTickets = lotteryTicketMapper.selectByExample(example);
        
        if (availableTickets.isEmpty()) {
            return null;
        }
        
        // ????????
        LotteryTicket selected = availableTickets.get(random.nextInt(availableTickets.size()));
        return selected.getTicketNumber();
    }

    // ==================== 末獎檢查與發放 ====================

    /**
     * 檢查是否已無可用票（AVAILABLE），若已無票則發放設定為「末獎」(isLastPrize=1) 的獎項給最後的使用者。
     *
     * 流程：
     * 1. 檢查是否還有 AVAILABLE 狀態的票；若有則不發放末獎。
     * 2. 查詢所有標註為末獎的獎項，對每個獎項按照數量發放到得獎者的獎箱。
     * 3. 將末獎 remaining 設為 0 並記錄發放日誌。
     *
     * @param lottery     當前抽獎活動
     * @param lastUserId  應發放末獎的使用者 ID
     * @return 已發放的末獎清單
     */
    private List<LotteryPrize> checkAndAwardLastPrize(Lottery lottery, String lastUserId) {
        // 1. 檢查是否有 AVAILABLE 狀態的票
        LotteryTicketExample availableCheck = new LotteryTicketExample();
        availableCheck.createCriteria()
                .andLotteryIdEqualTo(lottery.getId())
                .andStatusEqualTo("AVAILABLE");
        long remainingCount = lotteryTicketMapper.countByExample(availableCheck);

        if (remainingCount > 0) {
            return List.of(); // 尚有可用票，無法發放末獎
        }

        // 2. ??????????????????????????
        LotteryPrizeExample lastPrizeExample = new LotteryPrizeExample();
        lastPrizeExample.createCriteria()
                .andLotteryIdEqualTo(lottery.getId())
                .andIsLastPrizeEqualTo((byte) 1);
        List<LotteryPrize> lastPrizes = lotteryPrizeMapper.selectByExample(lastPrizeExample);

        if (lastPrizes.isEmpty()) {
            return List.of(); // 無末獎可發放
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
                    log.error("發放末獎時發生錯誤: prizeId={}, error={}", lastPrize.getId(), e.getMessage());
                }
            }
            // ????????????? remaining ??0
            lastPrize.setRemaining(0);
            lastPrize.setUpdatedAt(LocalDateTime.now());
            lotteryPrizeMapper.updateByPrimaryKey(lastPrize);

            awarded.add(lastPrize);
            log.info("?? ????????????????: userId={}, prizeId={}, prizeName={}, qty={}",
                    lastUserId, lastPrize.getId(), lastPrize.getName(), qty);
        }

        return awarded;
    }

    // ==================== ???????????? ====================

    @Override
    @Transactional
    public SessionInfo getOrCreateSession(String lotteryId, String userId) {
        log.info("[取得或建立 Session] lotteryId={}, userId={}", lotteryId, userId);
        
        // ???????????????????????????
        LotterySessionExample example = new LotterySessionExample();
        example.createCriteria()
                .andLotteryIdEqualTo(lotteryId)
                .andStatusEqualTo("ACTIVE");
        List<LotterySession> activeSessions = lotterySessionMapper.selectByExample(example);
        
        if (!activeSessions.isEmpty()) {
            // ??????????????????
            LotterySession activeSession = activeSessions.get(0);
            syncPlayerDesignationFromTickets(lotteryId, activeSession);
            
            // ???????????????????????????????????????????????????????? EXPIRED???????????????????????????
                LocalDateTime now = LocalDateTime.now();
                boolean isProtectionExpired = activeSession.getProtectionEndTime() != null
                    && activeSession.getProtectionEndTime().isBefore(now)
                    && !activeSession.getOpenerUserId().equals(userId);
                if (isProtectionExpired) {
                log.info("[Session 過期] sessionId={}, protectionEnd={}",
                    activeSession.getId(), activeSession.getProtectionEndTime());
                activeSession.setStatus("EXPIRED");
                activeSession.setCompletedAt(now);
                activeSession.setUpdatedAt(now);
                lotterySessionMapper.updateByPrimaryKey(activeSession);
                // 已將過期 Session 標記為 EXPIRED
                } else {
            // ?? SCRATCH_PLAYER ????????????????????????????????10 ????????????????????????????
            Lottery lotteryForTimeout = lotteryMapper.selectByPrimaryKey(lotteryId);
                boolean isTimedOut = lotteryForTimeout != null
                    && GameModeEnum.SCRATCH_PLAYER.getCode().equals(lotteryForTimeout.getGameMode())
                    && activeSession.getDesignationDeadline() != null
                    && activeSession.getDesignationDeadline().isBefore(LocalDateTime.now())
                    && (activeSession.getPlayerDesignatedNumbers() == null
                            || activeSession.getPlayerDesignatedNumbers().isBlank());
            
            if (!isTimedOut) {
                boolean isOpener = activeSession.getOpenerUserId().equals(userId);
                log.info("????????????????????? sessionId={}, isOpener={}", activeSession.getId(), isOpener);
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
                        activeSession.getDesignationDeadline()  // ??
                );
            }
            
                // 設計逾時，將 Session 設為 EXPIRED
                log.info("[設計逾時] sessionId={}, deadline={}", 
                    activeSession.getId(), activeSession.getDesignationDeadline());
            activeSession.setStatus("EXPIRED");
            activeSession.setCompletedAt(LocalDateTime.now());
            activeSession.setUpdatedAt(LocalDateTime.now());
            lotterySessionMapper.updateByPrimaryKey(activeSession);
            // ??????????????????????????????????????????????????????
            } // end else (isProtectionExpired)
        }
        
        // ??????????????????????????????????
        Lottery lottery = lotteryMapper.selectByPrimaryKey(lotteryId);
        if (lottery == null) {
            throw new BusinessException("Lottery not found");
        }
        
        LotterySession newSession = new LotterySession();
        newSession.setId(UUID.randomUUID().toString());
        newSession.setLotteryId(lotteryId);
        newSession.setOpenerUserId(userId);
        newSession.setProtectionDraws(resolveFreeDrawLimit(lottery));
        // ?? ????????????????????????????????????????????????????????startProtection ?????
        newSession.setProtectionStartTime(null);
        newSession.setProtectionEndTime(null);
        
        newSession.setOpenerDrawCount(0);
        newSession.setOpenerTotalCost(0L);
        newSession.setFreeDrawEnabled(resolveSessionFreeDrawEnabled(lottery));
        newSession.setFreeDrawTriggered((byte) 0);
        newSession.setFreeDrawRefundAmount(0L);
        newSession.setStatus("ACTIVE");
        newSession.setCreatedAt(LocalDateTime.now());
        newSession.setUpdatedAt(LocalDateTime.now());
        
        // ?? SCRATCH_PLAYER ??????????????????秋ㄠ???????10 ?????????????????
        if (GameModeEnum.SCRATCH_PLAYER.getCode().equals(lottery.getGameMode())) {
            newSession.setDesignationDeadline(LocalDateTime.now().plusMinutes(10));
            List<Integer> persistedNumbers = getPersistedPlayerDesignatedNumbers(lotteryId);
            if (!persistedNumbers.isEmpty()) {
                newSession.setPlayerDesignatedNumbers(persistedNumbers.toString());
                newSession.setDesignationDeadline(null);
            }
            log.info("[SCRATCH_PLAYER 設定] designationDeadline= {}", newSession.getDesignationDeadline());
        }
        
        lotterySessionMapper.insert(newSession);
        
        log.info("?? ?????????????????????????????: sessionId={}, opener={}", 
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
                null,  // ????????????????憌??????
                newSession.getDesignationDeadline()  // ??
        );
    }

    @Override
    @Transactional
    public boolean canDrawNow(String lotteryId, String userId) {
        log.info("[canDrawNow] lotteryId={}, userId={}", lotteryId, userId);
        
        // ???????????????????????????
        LotterySessionExample example = new LotterySessionExample();
        example.createCriteria()
                .andLotteryIdEqualTo(lotteryId)
                .andStatusEqualTo("ACTIVE");
        List<LotterySession> activeSessions = lotterySessionMapper.selectByExample(example);
        
        if (activeSessions.isEmpty()) {
            log.info("[canDrawNow] 無活動中的 Session，允許抽獎");
            return true;
        }
        
        LotterySession activeSession = activeSessions.get(0);
        
        // ???????????????????????
        if (activeSession.getOpenerUserId().equals(userId)) {
            log.info("[canDrawNow] 開啟者可抽獎");
            return true;
        }
        
        // ?? ?????????????????????玩??????????????? ?????????
        if (activeSession.getProtectionEndTime() == null) {
            log.info("[canDrawNow] 尚未啟動保護，允許抽獎");
            return true;
        }
        
        // ??????????????????????????????????????
        LocalDateTime now = LocalDateTime.now();
        if (activeSession.getProtectionEndTime().isBefore(now)) {
            // ??????????????????????
            activeSession.setStatus("EXPIRED");
            activeSession.setCompletedAt(now);
            activeSession.setUpdatedAt(now);
            lotterySessionMapper.updateByPrimaryKey(activeSession);
            
            log.info("[canDrawNow] Session 已過期，sessionId={}", activeSession.getId());
            return true;
        }
        
        // ??????????????????????????????????????
        log.warn("[canDrawNow] 被保護機制阻擋: opener={}, protectionEnd={}", 
            activeSession.getOpenerUserId(), activeSession.getProtectionEndTime());
        return false;
    }

    @Override
    public void expireOldSessions() {
        log.info("[expireOldSessions] 執行逾期 Session 清理");
        // TODO: ????????????????
        // sessionMapper.expireByTime(LocalDateTime.now());
    }

    // ==================== ?????????====================

    @Override
    @Transactional
    public boolean checkAndTriggerFreeDraw(String sessionId, String prizeId) {
        log.info("[checkAndTriggerFreeDraw] sessionId={}, prizeId={}", sessionId, prizeId);
        
        // ??????Session
        LotterySession session = lotterySessionMapper.selectByPrimaryKey(sessionId);
        if (session == null) {
            log.warn("[checkAndTriggerFreeDraw] 找不到 Session: {}", sessionId);
            return false;
        }
        
        Lottery lottery = lotteryMapper.selectByPrimaryKey(session.getLotteryId());
        boolean freeDrawEnabled = isFreeDrawEnabled(session, lottery);
        if (!freeDrawEnabled) {
            log.info("Free draw is not enabled for this session");
            return false;
        }
        
        // ?????????????????????????
        if (session.getFreeDrawTriggered() != null && session.getFreeDrawTriggered() == 1) {
            log.info("??????????????");
            return false;
        }
        
        // ??????????????????????????
        Integer openerDrawCount = session.getOpenerDrawCount() != null ? session.getOpenerDrawCount() : 0;
        Integer protectionDraws = resolveFreeDrawLimit(session, lottery);
        
        if (openerDrawCount > protectionDraws) {
            log.info("?????????????????? {} > {}", openerDrawCount, protectionDraws);
            return false;
        }
        
        // ???????????????????
        if (prizeId == null) {
            log.info("No prize drawn, skip free draw trigger");
            return false;
        }
        
        LotteryPrize prize = lotteryPrizeMapper.selectByPrimaryKey(prizeId);
        if (prize == null || prize.getIsGrandPrize() == null || prize.getIsGrandPrize() != 1) {
            log.info("??????????憌???????: prizeId={}", prizeId);
            return false;
        }
        
        // ??????????
        Long refundAmount = session.getOpenerTotalCost() != null ? session.getOpenerTotalCost() : 0L;
        
        if (refundAmount > 0) {
            // ?????????????????
            try {
                walletService.addGold(
                        session.getOpenerUserId(), 
                        refundAmount, 
                        "FREE_DRAW_REFUND", 
                        sessionId, 
                        "FREE_DRAW_REFUND"
                );
                
                // ??????????????????
                consumptionRecordService.recordConsumption(
                        session.getOpenerUserId(),
                        "FREE_DRAW_REFUND",
                        session.getLotteryId(),
                        lottery != null ? lottery.getTitle() : null,
                        null,
                        null,
                        -refundAmount,  // ????????????????
                        0L,
                        "FREE_DRAW_REFUND"
                );
                
                log.info("???????????? userId={}, amount={}", session.getOpenerUserId(), refundAmount);
            } catch (Exception e) {
                log.error("??? ???????? {}", e.getMessage(), e);
                return false;
            }
        }
        
        // ????? Session ????
        session.setFreeDrawTriggered((byte) 1);
        session.setFreeDrawRefundAmount(refundAmount);
        session.setFreeDrawTriggeredAt(LocalDateTime.now());
        session.setFreeDrawPrizeId(prizeId);
        session.setUpdatedAt(LocalDateTime.now());
        lotterySessionMapper.updateByPrimaryKey(session);
        
        log.info("?????????????: sessionId={}, refundAmount={}", sessionId, refundAmount);
        return true;
    }

    // ==================== ???????????? ====================

    @Override
    public List<Integer> getAvailableTicketNumbers(String lotteryId) {
        log.info("?? ?????????????????????????: lotteryId={}", lotteryId);
        
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
        log.info("??? ????????????????????????? sessionId={}, numbers={}", sessionId, prizeNumbers);
        
        LotterySession session = lotterySessionMapper.selectByPrimaryKey(sessionId);
        if (session != null) {
            // ??List<Integer> ??????JSON ?????
            String numbersJson = prizeNumbers.toString();  // ???????????1,2,3]
            session.setPlayerDesignatedNumbers(numbersJson);
            session.setUpdatedAt(LocalDateTime.now());
            lotterySessionMapper.updateByPrimaryKey(session);
            
            log.info("????????????");
        }
    }

    @Override
    public com.group.admin.entity.Lottery getLottery(String lotteryId) {
        return lotteryMapper.selectByPrimaryKey(lotteryId);
    }

    @Override
    public List<Integer> getAvailableRevealedNumbers(String lotteryId) {
        log.info("?? ????????? revealedNumber ??????????? lotteryId={}", lotteryId);

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
        log.info("????????????????????? lotteryId={}", lotteryId);

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
        log.info("?? ???????????????????? lotteryId={}", lotteryId);

        LotteryPrizeExample example = new LotteryPrizeExample();
        example.createCriteria()
                .andLotteryIdEqualTo(lotteryId)
                .andIsGrandPrizeEqualTo((byte) 1);
        example.setOrderByClause("order_num ASC");
        return lotteryPrizeMapper.selectByExample(example);
    }

    /**
     * ?????????????????????????????????????? AVAILABLE ???????
     * <p>
     * ??????????????????d IS NULL AND is_designated_prize = 0 AND status = AVAILABLE
     * ?????????
     *   1. ??????????????????sGrandPrize != 1?????????????????????????prizeId ????????
     *   2. ?????????????玩?????????????
     *   3. ?????????????????????????
     *   4. ???????????????????????????
     */
    private void autoAssignNonGrandPrizes(String lotteryId) {
        log.info("???????????????????? lotteryId={}", lotteryId);

        // 1. ????????????????
        LotteryPrizeExample prizeEx = new LotteryPrizeExample();
        LotteryPrizeExample.Criteria prizeCriteria = prizeEx.createCriteria()
                .andLotteryIdEqualTo(lotteryId);
        // ????????????sGrandPrize = 1??
        List<LotteryPrize> allPrizes = lotteryPrizeMapper.selectByExample(prizeEx);
        List<PrizeSlot> nonGrandPool = new ArrayList<>();
        for (LotteryPrize prize : allPrizes) {
            if (prize.getIsGrandPrize() != null && prize.getIsGrandPrize() == 1) continue;
            if ("THANKS".equalsIgnoreCase(prize.getLevel())) continue; // ?????????????????????憌??? null prize
            int qty = prize.getQuantity() != null ? prize.getQuantity() : 0;
            for (int i = 0; i < qty; i++) {
                nonGrandPool.add(new PrizeSlot(prize.getId(), prize.getLevel()));
            }
        }
        Collections.shuffle(nonGrandPool, random);

        // 2. ??????????prizeId ??AVAILABLE ?????????rizeId ??null ??????????
        LotteryTicketExample ticketEx = new LotteryTicketExample();
        ticketEx.createCriteria()
                .andLotteryIdEqualTo(lotteryId)
                .andStatusEqualTo("AVAILABLE")
                .andIsDesignatedPrizeEqualTo((byte) 0)
                .andPrizeIdIsNull();
        ticketEx.setOrderByClause("ticket_number ASC");
        List<LotteryTicket> unassigned = lotteryTicketMapper.selectByExample(ticketEx);

        log.info("????????????????????????? {}????????????????: {}", nonGrandPool.size(), unassigned.size());

        // 3. ??????
        int assignCount = Math.min(nonGrandPool.size(), unassigned.size());
        for (int i = 0; i < assignCount; i++) {
            LotteryTicket ticket = unassigned.get(i);
            PrizeSlot slot = nonGrandPool.get(i);
            ticket.setPrizeId(slot.prizeId());
            ticket.setPrizeLevel(slot.level());
            ticket.setUpdatedAt(LocalDateTime.now());
            lotteryTicketMapper.updateByPrimaryKey(ticket);
        }

        log.info("???????????????? {} ???????? {} ????????????",
                assignCount, unassigned.size() - assignCount);
    }

    private String resolvePaymentType(Lottery lottery) {
        return "BONUS".equalsIgnoreCase(lottery.getPaymentType()) ? "BONUS" : "GOLD";
    }

    // ==================== ???????????? ====================

    private record PrizeSlot(String prizeId, String level) {}

    // ==================== ????????026-03-02??===================

    @Override
    public SessionInfo getActiveSession(String lotteryId, String userId) {
        log.info("?? ??????????????????????? lotteryId={}", lotteryId);
        
        LotterySessionExample example = new LotterySessionExample();
        example.createCriteria()
                .andLotteryIdEqualTo(lotteryId)
                .andStatusEqualTo("ACTIVE");
        List<LotterySession> sessions = lotterySessionMapper.selectByExample(example);
        
        if (sessions.isEmpty()) {
            log.info("No active session found");
            return null;
        }
        
        LotterySession session = sessions.get(0);
        syncPlayerDesignationFromTickets(lotteryId, session);
        
        // ??????????????????????????
        if (session.getProtectionEndTime() != null && session.getProtectionEndTime().isBefore(LocalDateTime.now())) {
            session.setStatus("EXPIRED");
            session.setCompletedAt(LocalDateTime.now());
            session.setUpdatedAt(LocalDateTime.now());
            lotterySessionMapper.updateByPrimaryKey(session);
            log.info("????????????????????????????? sessionId={}", session.getId());
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
                session.getDesignationDeadline()  // ??
        );
    }

    @Override
    public List<DesignatedWinningNumber> getDesignatedWinningNumbers(String lotteryId) {
        log.info("?? ????????????????????????? lotteryId={}", lotteryId);
        
        // ?????????isDesignatedPrize=1 ??????
        LotteryTicketExample ticketEx = new LotteryTicketExample();
        ticketEx.createCriteria()
                .andLotteryIdEqualTo(lotteryId)
                .andIsDesignatedPrizeEqualTo((byte) 1);
        ticketEx.setOrderByClause("revealed_number ASC");
        List<LotteryTicket> designatedTickets = lotteryTicketMapper.selectByExample(ticketEx);
        
        if (designatedTickets.isEmpty()) {
            log.info("No designated winning numbers found");
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
        
        log.info("Loaded designated winning numbers: {}", result.size());
        return result;
    }

    /**
     * ?????????????????????????????
     * <p>??session ??protectionStartTime/protectionEndTime ?????????????????????????+ protectionMinutes</p>
     */
    @Transactional
    public void startProtection(String sessionId, String lotteryId) {
        LotterySession session = lotterySessionMapper.selectByPrimaryKey(sessionId);
        if (session == null || session.getProtectionEndTime() != null) {
            return; // ??????????????
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
        
        log.info("?????????????????: sessionId={}, protectionEnd={}", sessionId, session.getProtectionEndTime());
    }

    /**
     * ??????????????????????API ????????????????????????????
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
            log.info("??????????????????? sessionId={}, newEndTime={}", sessionId, newEndTime);
        }
    }

    private boolean isFreeDrawEnabled(LotterySession session, Lottery lottery) {
        if (lottery != null) {
            if (lottery.getFreeDrawEnabled() != null && lottery.getFreeDrawEnabled() == 1) {
                return true;
            }
            Integer threshold = lottery.getFreeDrawThreshold();
            if (threshold != null && threshold >= 1) {
                return true;
            }
        }
        return session.getFreeDrawEnabled() != null && session.getFreeDrawEnabled() == 1;
    }

    private byte resolveSessionFreeDrawEnabled(Lottery lottery) {
        return isLotteryFreeDrawEnabled(lottery) ? (byte) 1 : (byte) 0;
    }

    private boolean isLotteryFreeDrawEnabled(Lottery lottery) {
        if (lottery == null) {
            return false;
        }
        if (lottery.getFreeDrawEnabled() != null && lottery.getFreeDrawEnabled() == 1) {
            return true;
        }
        Integer threshold = lottery.getFreeDrawThreshold();
        return threshold != null && threshold >= 1;
    }

    private Integer resolveFreeDrawLimit(Lottery lottery) {
        if (lottery == null) {
            return 0;
        }
        Integer threshold = lottery.getFreeDrawThreshold();
        if (threshold != null && threshold >= 1) {
            return threshold;
        }
        return lottery.getProtectionDraws() != null ? lottery.getProtectionDraws() : 0;
    }

    private Integer resolveFreeDrawLimit(LotterySession session, Lottery lottery) {
        Integer lotteryLimit = resolveFreeDrawLimit(lottery);
        if (lotteryLimit != null && lotteryLimit >= 1) {
            return lotteryLimit;
        }
        return session.getProtectionDraws() != null ? session.getProtectionDraws() : 0;
    }

    /**
     * ????????????????????Controller ???? synchronized??
     */
    public Object getGachaLock(String lotteryId) {
        return gachaLocks.computeIfAbsent(lotteryId, k -> new Object());
    }

    @Override
    public DesignationCheckResponse getDesignationStatus(String lotteryId, String userId) {
        log.info("?? ???????????? lotteryId={}, userId={}", lotteryId, userId);

        Lottery lottery = lotteryMapper.selectByPrimaryKey(lotteryId);
        if (lottery == null) {
            return DesignationCheckResponse.builder().required(false).build();
        }

        // ??SCRATCH_PLAYER ????? ?????????????????
        if (!GameModeEnum.SCRATCH_PLAYER.getCode().equals(lottery.getGameMode())) {
            return DesignationCheckResponse.builder()
                    .required(false)
                    .gameMode(lottery.getGameMode())
                    .build();
        }

        // ??????ACTIVE session
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
        syncPlayerDesignationFromTickets(lotteryId, session);
        boolean isOpener = userId != null && userId.equals(session.getOpenerUserId());

        // ??????????
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

        // ?????????????
        if (isOpener) {
            // ????????????
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

            // ????????? revealedNumbers
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
            // ????????????????????????????????????
            return DesignationCheckResponse.builder()
                    .required(true)
                    .gameMode(lottery.getGameMode())
                    .sessionId(session.getId())
                    .isOpener(false)
                    .message("Please wait for opener designation")
                    .build();
        }
    }
}
