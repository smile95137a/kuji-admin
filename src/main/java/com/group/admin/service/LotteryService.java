package com.group.admin.service;

import com.group.admin.req.common.QueryReq;
import com.group.admin.req.lottery.LotteryCondition;
import com.group.admin.req.lottery.LotteryCreateReq;
import com.group.admin.req.lottery.LotteryUpdateReq;
import com.group.admin.res.lottery.LotteryRes;

import java.util.List;
import java.util.Map;

/**
 * 抽獎商品服務介面
 *
 * @author KUJI System
 * @since 1.0.0
 */
public interface LotteryService {
    

    // ==================== 降價機制 ====================

    /**
     * 觸發大獎售完降價（由系統自動調用）
     *
     * @param lotteryId 商品ID
     */
    void triggerGrandPrizeDiscount(String lotteryId);

    /**
     * 檢查是否需要觸發降價
     *
     * @param lotteryId 商品ID
     * @return 是否觸發降價
     */
    boolean checkAndTriggerDiscount(String lotteryId);

    // ==================== 統計相關 ====================

    /**
     * 獲取商品的剩餘抽數（從獎池計算）
     *
     * @param lotteryId 商品ID
     * @return 剩餘抽數
     */
    int getRemainingDrawCount(String lotteryId);

    /**
     * 獲取商品的獎項統計
     *
     * @param lotteryId 商品ID
     * @return 統計資訊（總數、剩餘數等）
     */
    Map<String, Object> getPrizeStatistics(String lotteryId);

    /**
     * 根據商品 ID 查詢獎品列表
     *
     * @param lotteryId 商品ID
     * @return 獎品列表
     */
    List<com.group.admin.res.lottery.LotteryPrizeRes> getPrizesByLotteryId(String lotteryId);
    
    // ==================== 新架構 API（Condition + QueryReq 模式）====================
    
    /**
     * 查詢商品列表（新架構）
     * 
     * 使用 Condition + QueryReq 模式
     * 所有查詢條件可選，MyBatis 動態 SQL
     * 返回全部資料，前端做分頁
     * 
     * @param req 查詢請求（可選）
     * @return 商品列表（全部資料）
     */
    List<LotteryRes> queryLotteries(QueryReq<LotteryCondition> req);
    
    /**
     * 新增商品（新架構）
     * 
     * storeId 已經在 Controller 自動帶入
     * 
     * @param req 商品建立請求
     * @return 建立的商品
     */
    LotteryRes createLottery(LotteryCreateReq req);
    
    /**
     * 更新商品（新架構）
     * 
     * @param id 商品 ID
     * @param req 更新請求
     * @return 更新後的商品
     */
    LotteryRes updateLottery(String id, LotteryUpdateReq req);
    
    /**
     * 刪除商品（新架構）
     * 
     * @param id 商品 ID
     */
    void deleteLottery(String id);
    
    /**
     * 取得商品詳情（新架構）
     * 
     * @param id 商品 ID
     * @return 商品詳情
     */
    LotteryRes getLottery(String id);
    
    /**
     * 更新商品狀態（新架構）
     * 
     * @param id 商品 ID
     * @param status ON_SHELF / OFF_SHELF
     * @return 更新後的商品
     */
    LotteryRes updateStatus(String id, String status);
    
    /**
     * 複製商品（完整複製）
     * 
     * 複製內容包含：
     * - Lottery 主表資料（產生新 ID、更新標題）
     * - 所有 LotteryPrize（獎項）
     * - 可選擇是否重新生成籤號
     * 
     * @param sourceLotteryId 來源商品 ID
     * @param newTitle 新商品標題（選填）
     * @param regenerateTickets 是否重新生成籤號
     * @param newStatus 新商品狀態（選填，預設 OFF_SHELF）
     * @return 複製後的商品
     */
    LotteryRes copyLottery(String sourceLotteryId, String newTitle, Boolean regenerateTickets, String newStatus);
    
    // ==================== 整合 API（商品+獎品一起操作）====================
    
    /**
     * 建立商品並同時新增獎品列表
     * 
     * 一支 API 完成：
     * 1. 建立商品（Lottery）
     * 2. 批次新增獎品（LotteryPrize）
     * 3. 返回完整資料（包含獎品列表）
     * 
     * @param req 商品與獎品整合建立請求
     * @param operatorId 操作者 ID
     * @return 商品與獎品完整資訊
     */
    com.group.admin.res.lottery.LotteryWithPrizesRes createLotteryWithPrizes(
            com.group.admin.req.lottery.LotteryWithPrizesCreateReq req, 
            String operatorId);
    
    /**
     * 更新商品並同時更新獎品列表
     * 
     * 一支 API 完成：
     * 1. 更新商品（Lottery）
     * 2. 更新/新增獎品（LotteryPrize）
     *    - 有 ID 的獎品 → 更新
     *    - 沒有 ID 的獎品 → 新增
     * 3. 返回完整資料（包含獎品列表）
     * 
     * @param req 商品與獎品整合更新請求
     * @param operatorId 操作者 ID
     * @return 商品與獎品完整資訊
     */
    com.group.admin.res.lottery.LotteryWithPrizesRes updateLotteryWithPrizes(
            com.group.admin.req.lottery.LotteryWithPrizesUpdateReq req, 
            String operatorId);
    
    /**
     * 查詢商品詳情（包含獎品列表）
     * 
     * 一支 API 返回：
     * - 商品基本資訊
     * - 所有獎品列表
     * - 統計資訊（總數量、剩餘數量、進度）
     * 
     * @param lotteryId 商品 ID
     * @return 商品與獎品完整資訊
     */
    com.group.admin.res.lottery.LotteryWithPrizesRes getLotteryWithPrizes(String lotteryId);
    
    /**
     * 查詢所有商品列表（包含獎品列表）
     * 
     * 一支 API 返回：
     * - 所有商品及其獎品的完整資訊
     * - 支援查詢條件過濾
     * - 每個商品包含完整的獎品列表和統計資訊
     * 
     * @param req 查詢請求（可選的查詢條件）
     * @return 商品與獎品完整資訊列表
     */
    List<com.group.admin.res.lottery.LotteryWithPrizesRes> getAllLotteriesWithPrizes(
            com.group.admin.req.common.QueryReq<com.group.admin.req.lottery.LotteryCondition> req);
    
    // ==================== 熱度管理 ====================
    
    /**
     * 增加商品熱度（hotCount +1）
     * 
     * 使用情境：
     * - 使用者進入商品詳情頁時呼叫
     * - 使用者點擊商品時呼叫
     * 
     * @param lotteryId 商品 ID
     * @return 更新後的 hotCount
     */
    int incrementHotCount(String lotteryId);
    
    /**
     * 變更商品狀態（含 FSM 轉換驗證）
     * 
     * 合法轉換：
     * DRAFT → ON_SHELF, ON_SHELF → OFF_SHELF, OFF_SHELF → ON_SHELF,
     * DRAFT → CONFIGURED, CONFIGURED → ON_SHELF, ANY → FORCED_OFF,
     * FORCED_OFF → DRAFT
     * 
     * @param lotteryId    商品 ID
     * @param targetStatus 目標狀態
     * @param reason       原因（強制下架時使用）
     * @param operatorId   操作者 ID
     * @return 更新後的商品
     */
    LotteryRes changeStatus(String lotteryId, String targetStatus, String reason, String operatorId);
}
