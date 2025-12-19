package com.group.admin.service;

import com.group.admin.req.lottery.LotteryPrizeCreateReq;
import com.group.admin.req.lottery.LotteryPrizeUpdateReq;
import com.group.admin.res.lottery.LotteryPrizeRes;

import java.util.List;

/**
 * 獎項管理服務介面
 * 所有 ID 均為 UUID (String)
 *
 * @author KUJI System
 * @since 1.0.0
 */
public interface LotteryPrizeService {

    /**
     * 建立獎項
     *
     * @param req 建立請求
     * @return 建立後的獎項資料
     */
    LotteryPrizeRes createPrize(LotteryPrizeCreateReq req);

    /**
     * 批量建立獎項
     *
     * @param lotteryId 抽獎活動ID
     * @param reqList   獎項建立請求列表
     * @return 建立後的獎項列表
     */
    List<LotteryPrizeRes> createPrizes(String lotteryId, List<LotteryPrizeCreateReq> reqList);

    /**
     * 更新獎項
     *
     * @param req 更新請求
     * @return 更新後的獎項資料
     */
    LotteryPrizeRes updatePrize(LotteryPrizeUpdateReq req);

    /**
     * 刪除獎項
     *
     * @param id 獎項ID
     */
    void deletePrize(String id);

    /**
     * 根據ID查詢獎項
     *
     * @param id 獎項ID
     * @return 獎項資料
     */
    LotteryPrizeRes getPrizeById(String id);

    /**
     * 根據抽獎活動ID查詢所有獎項
     *
     * @param lotteryId 抽獎活動ID
     * @return 獎項列表
     */
    List<LotteryPrizeRes> getPrizesByLotteryId(String lotteryId);

    /**
     * 根據等級查詢獎項
     *
     * @param lotteryId 抽獎活動ID
     * @param level     等級
     * @return 獎項列表
     */
    List<LotteryPrizeRes> getPrizesByLevel(String lotteryId, String level);

    /**
     * 重置獎項剩餘數量（將 remaining 設為 quantity）
     *
     * @param lotteryId 抽獎活動ID
     */
    void resetPrizeRemaining(String lotteryId);

    /**
     * 查詢可選號碼清單（刮刮樂模式使用）
     *
     * @param lotteryId 抽獎活動ID
     * @return 可選號碼列表
     */
    List<String> getAvailableNumbers(String lotteryId);
}
