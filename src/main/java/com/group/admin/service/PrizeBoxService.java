package com.group.admin.service;

import com.group.admin.req.prizebox.PrizeBoxRecycleReq;
import com.group.admin.req.prizebox.PrizeBoxShipReq;
import com.group.admin.res.PageResult;
import com.group.admin.res.prizebox.PrizeBoxItemRes;
import com.group.admin.res.prizebox.PrizeBoxSummaryRes;
import com.group.admin.res.prizebox.RecycleResultRes;

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
     */
    void addToPrizeBox(String userId, String lotteryId, String prizeId, String storeId, Long recycleBonus);
    
    /**
     * 查詢玩家的獎品盒（所有未出貨的獎品）
     */
    List<PrizeBoxItemRes> getPrizeBox(String userId);
    
    /**
     * 查詢玩家的獎品盒（按狀態過濾）
     */
    List<PrizeBoxItemRes> getPrizeBox(String userId, String status);
    
    /**
     * 查詢獎品盒歷史紀錄（已出貨/已回收）
     *
     * @param userId 玩家 ID
     * @param status 狀態過濾（SHIPPED/RECYCLED/COMPLETED，null 表示全部歷史）
     * @param page   頁碼（從 1 開始）
     * @param size   每頁筆數
     * @return 分頁結果
     */
    PageResult<PrizeBoxItemRes> getPrizeBoxHistory(String userId, String status, int page, int size);
    
    /**
     * 按店家分組查詢獎品盒（用於出貨選擇）
     */
    List<PrizeBoxSummaryRes> getSummaryByStore(String userId);
    
    /**
     * 出貨（將選定的獎品產生訂單）
     */
    List<String> shipPrizes(String userId, PrizeBoxShipReq req);
    
    /**
     * 回收獎品（轉換為紅利）
     *
     * @return 回收結果（含總紅利和回收數量）
     */
    RecycleResultRes recyclePrizes(String userId, PrizeBoxRecycleReq req);
    
    /**
     * 取得單一獎品盒項目詳情
     */
    PrizeBoxItemRes getPrizeBoxItem(String prizeBoxId);
}
