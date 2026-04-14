package com.group.admin.service;

import com.group.admin.req.lottery.LotteryCreateReq;
import com.group.admin.res.lottery.DesignationCheckResponse;
import com.group.admin.res.lottery.LotteryTicketRes;
import com.group.admin.res.lottery.TicketListResponse;

import java.util.List;

/**
 * 籤位服務介面
 * 
 * <p>負責籤位生成、抽獎邏輯、免單機制等核心功能</p>
 * 
 * <h3>支援的遊戲模式：</h3>
 * <ul>
 *   <li>RANDOM - 一番賞/扭蛋/卡牌（隨機分配獎品到籤位）</li>
 *   <li>SCRATCH_STORE - 刮刮樂（店家指定大獎位置）</li>
 *   <li>SCRATCH_PLAYER - 刮刮樂（開套玩家指定大獎位置）</li>
 * </ul>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
public interface LotteryTicketService {

    // ==================== 籤位生成 ====================

    /**
     * 生成籤位（商品上架時呼叫）
     * 
     * <p>根據遊戲模式生成對應的籤位：</p>
     * <ul>
     *   <li>RANDOM: 根據各獎品數量隨機分配到籤位</li>
     *   <li>SCRATCH_STORE: 根據店家指定的號碼設定大獎位置</li>
     *   <li>SCRATCH_PLAYER: 只生成空籤位，等待開套玩家指定</li>
     * </ul>
     * 
     * @param lotteryId 抽獎活動 ID
     */
    void generateTickets(String lotteryId);

    /**
     * 刮刮樂(玩家指定)：開套玩家指定大獎位置
     * 
     * @param lotteryId 抽獎活動 ID
     * @param userId 開套玩家 ID
     * @param designations 大獎指定列表（籤位號碼 → 獎品 ID）
     */
    void designatePrizePositions(String lotteryId, String userId, List<PrizeDesignation> designations);
    
    /**
     * 大獎指定 DTO
     * ticketNumber 已變更為 revealedNumber：玩家指定對應瀏樂 revealed_number 欄位的大獎
     */
    record PrizeDesignation(Integer revealedNumber, String prizeId) {}

    // ==================== 前台籤位查詢（安全版本）====================

    /**
     * 取得籤位列表（前台用，隱藏未抽籤位的獎品資訊）
     *
     * <p>⚠️ 安全規則：</p>
     * <ul>
     *   <li>未抽籤位：只返回 number + status</li>
     *   <li>已抽籤位：返回完整獎品資訊</li>
     * </ul>
     *
     * @param lotteryId 抽獎活動 ID
     * @return 籤位列表（已過濾敏感資訊）
     */
    List<LotteryTicketRes> getTicketsForFrontend(String lotteryId);

    /**
     * 取得帶統計資訊的籤位列表（前台用）
     *
     * <p>與 {@link #getTicketsForFrontend} 相同的資訊隱藏規則，
     * 但回傳結構包含 lotteryId、gameMode、totalTickets、availableCount、drawnCount。</p>
     *
     * @param lotteryId 抽獎活動 ID
     * @return TicketListResponse（含統計與 TicketView 列表）
     */
    TicketListResponse getTicketList(String lotteryId);

    /**
     * 查詢 SCRATCH_PLAYER 模式的大獎指定狀態（前台輪詢用）
     *
     * <p>四種情境回應：</p>
     * <ol>
     *   <li>非 SCRATCH_PLAYER → required=false</li>
     *   <li>無 ACTIVE Session 或已指定完成 → required=false, alreadyDesignated</li>
     *   <li>開套玩家尚未指定 → required=true, isOpener=true，含大獎清單與可選號碼</li>
     *   <li>非開套玩家尚未指定 → required=true, isOpener=false，含等待訊息</li>
     * </ol>
     *
     * @param lotteryId 抽獎活動 ID
     * @param userId    當前玩家 ID
     * @return DesignationCheckResponse
     */
    DesignationCheckResponse getDesignationStatus(String lotteryId, String userId);

    /**
     * 取得籤位列表（後台用，完整資訊）
     * 
     * @param lotteryId 抽獎活動 ID
     * @return 籤位列表（包含所有資訊）
     */
    List<LotteryTicketRes> getTicketsForBackend(String lotteryId);

    // ==================== 抽獎核心邏輯 ====================

    /**
     * 執行抽獎（統一入口）
     * 
     * <p>完整流程：</p>
     * <ol>
     *   <li>檢查商品狀態（上架中、有剩餘籤位）</li>
     *   <li>檢查/建立開套場次</li>
     *   <li>檢查保護時間（是否被其他玩家鎖定）</li>
     *   <li>檢查使用者餘額</li>
     *   <li>執行抽獎（選號或隨機）</li>
     *   <li>更新籤位狀態</li>
     *   <li>扣款</li>
     *   <li>檢查免單條件</li>
     *   <li>記錄抽獎紀錄</li>
     *   <li>返回結果（揭露獎品資訊）</li>
     * </ol>
     * 
     * @param lotteryId 抽獎活動 ID
     * @param userId 玩家 ID
     * @param ticketNumber 選擇的籤位編號（null=隨機抽）
     * @param drawCount 抽獎次數（連抽用）
     * @return 抽獎結果
     */
    DrawResult draw(String lotteryId, String userId, Integer ticketNumber, int drawCount);

    /**
     * 依照 ticketId 抽籤（玩家指定特定票券的情況）
     *
     * @param lotteryId 抽獎活動 ID
     * @param userId 玩家 ID
     * @param ticketId 籤位 UUID
     * @return 抽獎結果
     */
    DrawResult drawByTicketId(String lotteryId, String userId, String ticketId);

    /**
     * 隨機抽一個可用籤位
     * 
     * @param lotteryId 抽獎活動 ID
     * @return 抽中的籤位編號
     */
    Integer getRandomAvailableTicket(String lotteryId);
    
    /**
     * 取得可用籤位編號列表（劃劃樂指定用）
     * 
     * @param lotteryId 抽獎活動 ID
     * @return 可用籤位編號列表
     */
    List<Integer> getAvailableTicketNumbers(String lotteryId);
    
    /**
     * 標記玩家已指定大獎位置
     * 
     * @param sessionId 場次 ID
     * @param prizeNumbers 指定的大獎編號列表
     */
    void markPlayerDesignated(String sessionId, List<Integer> prizeNumbers);
    
    /**
     * 取得可用的 revealedNumber 列表（刮刮樂玩家指定大獎用）
     * 回傳所有 status=AVAILABLE 的籤位的 revealedNumber（去重排序）
     *
     * @param lotteryId 抽獎活動 ID
     * @return revealedNumber 列表
     */
    List<Integer> getAvailableRevealedNumbers(String lotteryId);

    /**
     * 取得已指定的大獎中獎號碼清單（前台公開顯示用）
     * <p>
     * 顯示哪些 revealedNumber 是大獎，以及對應的獎品資訊。
     * 讓所有玩家知道「刮中幾號能得到大獎」。
     * </p>
     *
     * @param lotteryId 抽獎活動 ID
     * @return 已指定的中獎號碼清單
     */
    List<DesignatedWinningNumber> getDesignatedWinningNumbers(String lotteryId);

    /**
     * 取得目前進行中的場次（唯讀，不建立新場次）
     * <p>用於 getTickets 等唯讀場景，避免查看就建立場次/啟動保護時間</p>
     *
     * @param lotteryId 抽獎活動 ID
     * @param userId 當前使用者 ID（判斷是否為開套者）
     * @return 場次資訊，若無進行中場次則回傳 null
     */
    SessionInfo getActiveSession(String lotteryId, String userId);

    /**
     * 取得大獎獎品清單（isGrandPrize=1）
     *
     * @param lotteryId 抽獎活動 ID
     * @return 大獎獎品列表
     */
    List<com.group.admin.entity.LotteryPrize> getGrandPrizes(String lotteryId);

    /**
     * 取得抽獎活動資訊
     * 
     * @param lotteryId 抽獎活動 ID
     * @return 抽獎活動實體
     */
    com.group.admin.entity.Lottery getLottery(String lotteryId);

    // ==================== 開套場次管理 ====================

    /**
     * 取得或建立開套場次
     * 
     * <p>如果當前沒有有效場次，建立新場次並設定保護時間</p>
     * 
     * @param lotteryId 抽獎活動 ID
     * @param userId 玩家 ID
     * @return 場次資訊
     */
    SessionInfo getOrCreateSession(String lotteryId, String userId);

    /**
     * 檢查是否在保護時間內（其他玩家不能抽）
     * 
     * @param lotteryId 抽獎活動 ID
     * @param userId 當前玩家 ID
     * @return true=可以抽, false=被其他玩家鎖定
     */
    boolean canDrawNow(String lotteryId, String userId);

    /**
     * 處理過期的場次（定時任務呼叫）
     */
    void expireOldSessions();

    // ==================== 免單機制 ====================

    /**
     * 檢查並處理免單
     * 
     * <p>條件：開套玩家 + 保護期內 + 中大獎</p>
     * 
     * @param sessionId 場次 ID
     * @param prizeId 中獎的獎品 ID
     * @return 是否觸發免單
     */
    boolean checkAndTriggerFreeDraw(String sessionId, String prizeId);

    // ==================== 內部類別 ====================

    /**
     * 抽獎結果
     */
    record DrawResult(
        boolean success,
        String ticketId,
        int ticketNumber,
        Integer revealedNumber,   // 刮刮樂專用：刮開後揭露的號碼；一番賞/扭蛋為 null
        String prizeId,
        String prizeLevel,
        String prizeName,
        String prizeImageUrl,
        boolean isGrandPrize,
        boolean triggeredFreeDraw,
        Long refundAmount,
        String message,
        boolean lastPrizeAwarded,    // 最後賞是否已自動發放（此籤為全套最後一抽）
        String lastPrizeId,          // 自動發放的最後賞 ID（null 表示未觸發）
        String lastPrizeName,        // 自動發放的最後賞名稱
        String lastPrizeImageUrl     // 自動發放的最後賞圖片
    ) {}

    /**
     * 場次資訊
     */
    record SessionInfo(
        String sessionId,
        String lotteryId,
        String openerUserId,
        boolean isOpener,
        int protectionDraws,
        java.time.LocalDateTime protectionEndTime,
        int openerDrawCount,
        boolean freeDrawEnabled,
        String status,
        String playerDesignatedNumbers,  // 玩家指定的大獎位置（JSON 格式）
        java.time.LocalDateTime designationDeadline  // 開套者指定大獎的截止時間（SCRATCH_PLAYER 模式）
    ) {}

    /**
     * 已指定的大獎中獎號碼 DTO（前台顯示用）
     * <p>告知所有玩家哪些 revealedNumber 是大獎、對應什麼獎品</p>
     */
    record DesignatedWinningNumber(
        Integer revealedNumber,
        String prizeId,
        String prizeName,
        String prizeLevel,
        String prizeImageUrl
    ) {}
}
