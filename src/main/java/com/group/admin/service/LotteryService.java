package com.group.admin.service;

import com.group.admin.entity.LotteryDrawRecord;
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
    
    // ==================== 商品管理 CRUD ====================
    
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
}
