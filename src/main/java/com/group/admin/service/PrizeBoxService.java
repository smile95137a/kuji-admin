package com.group.admin.service;

import com.group.admin.req.prizebox.PrizeBoxRecycleReq;
import com.group.admin.req.prizebox.PrizeBoxShipReq;
import com.group.admin.res.PageResult;
import com.group.admin.res.prizebox.PrizeBoxItemRes;
import com.group.admin.res.prizebox.PrizeBoxSummaryRes;
import com.group.admin.res.prizebox.RecycleResultRes;
import com.group.admin.res.order.OrderPaymentInitRes;

import java.util.List;

/**
 * 獎品盒服務介面
 * 管理玩家抽中的獎品
 *
 * @author Kuji Admin
 * @since 2026-01-09
 */
public interface PrizeBoxService {

    /**
     * 新增獎品到獎品盒（抽獎後自動執行）
     *
     * @param userId      玩家 ID
     * @param lotteryId   一番賞 ID
     * @param prizeId     獎品 ID
     * @param storeId     店家 ID
     * @param recycleBonus 回收可得紅利
     */
    void addToPrizeBox(String userId, String lotteryId, String prizeId, String storeId, Long recycleBonus);

    /**
     * 查詢玩家的獎品盒（所有未出貨的獎品）
     *
     * @param userId 玩家 ID
     * @return 獎品列表
     */
    List<PrizeBoxItemRes> getPrizeBox(String userId);

    /**
     * 分頁查詢使用者目前仍在賞品盒中的項目。
     *
     * @param userId 使用者 ID
     * @param page   頁碼，從 1 開始
     * @param size   每頁筆數
     * @return 分頁後的賞品盒項目
     */
    PageResult<PrizeBoxItemRes> getPrizeBoxPage(String userId, int page, int size);

    /**
     * 按店家分組查詢獎品盒（用於出貨選擇）
     *
     * @param userId 玩家 ID
     * @return 按店家分組的獎品摘要
     */
    List<PrizeBoxSummaryRes> getSummaryByStore(String userId);

    /**
     * 出貨（將選定的獎品產生訂單）
     * 會自動按店家拆分訂單
     *
     * @param userId 玩家 ID
     * @param req    出貨請求（包含獎品列表、收件資訊等）
     * @return 訂單 ID 列表
     */
    List<OrderPaymentInitRes> shipPrizes(String userId, PrizeBoxShipReq req);

    /**
     * 回收獎品（轉換為紅利）
     *
     * @param userId 玩家 ID
     * @param req    回收請求（包含獎品列表）
     * @return 回收結果（totalBonus、recycledCount）
     */
    RecycleResultRes recyclePrizes(String userId, PrizeBoxRecycleReq req);

    /**
     * 取得單一獎品盒項目詳情
     *
     * @param prizeBoxId 獎品盒 ID
     * @return 獎品詳情
     */
    PrizeBoxItemRes getPrizeBoxItem(String prizeBoxId);

    /**
     * 查詢玩家獎品盒歷史（所有狀態）
     *
     * @param userId 玩家 ID
     * @param status 狀態篩選（null = 全部）
     * @param page   頁碼（從 1 開始）
     * @param size   每頁筆數
     * @return 分頁獎品列表
     */
    PageResult<PrizeBoxItemRes> getPrizeBoxHistory(String userId, String status, int page, int size);
}
