package com.group.admin.service;

import com.group.admin.entity.LotteryDrawRecord;
import com.group.admin.req.common.QueryReq;
import com.group.admin.req.lottery.LotteryCondition;
import com.group.admin.req.lottery.LotteryCreateReq;
import com.group.admin.req.lottery.LotteryQueryReq;
import com.group.admin.req.lottery.LotteryUpdateReq;
import com.group.admin.res.lottery.LotteryListRes;
import com.group.admin.res.lottery.LotteryRes;
import com.group.admin.res.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 抽獎商品服務介面
 *
 * @author KUJI System
 * @since 1.0.0
 */
public interface LotteryService {
    
    // ==================== 排程觸發 ====================

    /**
     * 自動將排程到期商品從 CONFIGURED 推進到 ON_SHELF
     */
    void promoteScheduledLotteries();

    /**
     * 自動將 ON_SHELF 商品推進到 DRAWABLE（開放抽獎）
     */
    void promoteDrawableLotteries();

    /**
     * 建立抽獎商品
     *
     * @param req       建立請求
     * @param operatorId 操作者ID
     * @return 建立後的商品資料
     */
    LotteryRes createLottery(LotteryCreateReq req, String operatorId);

    /**
     * 更新抽獎商品
     *
     * @param req       更新請求
     * @param operatorId 操作者ID
     * @return 更新後的商品資料
     */
    LotteryRes updateLottery(LotteryUpdateReq req, String operatorId);

    /**
     * 刪除抽獎商品（僅限草稿或已下架狀態）
     *
     * @param id         商品ID
     * @param operatorId 操作者ID
     */
    void deleteLottery(String id, String operatorId);

    /**
     * 根據ID查詢商品詳情
     *
     * @param id 商品ID
     * @return 商品詳情
     */
    LotteryRes getLotteryById(String id);

    /**
     * 分頁查詢商品列表
     *
     * @param req 查詢請求
     * @return 商品列表分頁結果
     */
    PageResult<LotteryListRes> queryLotteries(LotteryQueryReq req);

    /**
     * 根據店家ID查詢商品列表（給 StoreOwner 使用）
     *
     * @param storeId 店家ID
     * @param req     查詢請求
     * @return 商品列表分頁結果
     */
    PageResult<LotteryListRes> queryLotteriesByStore(String storeId, LotteryQueryReq req);

    // ==================== 狀態管理 ====================

    /**
     * 上架商品
     *
     * @param id         商品ID
     * @param operatorId 操作者ID
     * @return 更新後的商品資料
     */
    LotteryRes publishLottery(String id, String operatorId);

    /**
     * 下架商品
     *
     * @param id         商品ID
     * @param operatorId 操作者ID
     * @return 更新後的商品資料
     */
    LotteryRes unpublishLottery(String id, String operatorId);

    /**
     * 強制下架商品
     *
     * @param id         商品ID
     * @param reason     下架原因
     * @param operatorId 操作者ID
     * @return 更新後的商品資料
     */
    LotteryRes forceOffShelf(String id, String reason, String operatorId);

    // ==================== 抽獎相關 ====================

    /**
     * 執行抽獎
     *
     * @param lotteryId 抽獎活動 ID
     * @param userId    使用者 ID
     * @param costType  消費類型 (gold/bonus)
     * @return 抽獎記錄
     */
    LotteryDrawRecord draw(String lotteryId, String userId, String costType);

    /**
     * 執行多連抽
     *
     * @param lotteryId  抽獎活動 ID
     * @param userId     使用者 ID
     * @param drawCount  連抽次數
     * @param costType   消費類型 (gold/bonus)
     * @return 抽獎記錄列表
     */
    List<LotteryDrawRecord> multiDraw(String lotteryId, String userId, int drawCount, String costType);

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
     * 所有查詢條件可選，回傳後端分頁結果
     *
     * @param req 查詢請求（可選）
     * @return 商品列表分頁結果
     */
    PageResult<LotteryRes> queryLotteries(QueryReq<LotteryCondition> req);
    
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

    /**
     * 根據下架策略自動檢查並下架商品
     * 
     * 策略說明：
     * - GRAND_PRIZE_DRAWN：所有大獎抽完後自動下架
     * - ALL_DRAWN：全部籤位抽完後自動下架
     * - MANUAL：不自動下架（手動控制）
     * 
     * @param lotteryId 商品 ID
     */
    void checkAndDelist(String lotteryId);
}
