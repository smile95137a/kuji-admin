package com.group.admin.service;

import com.group.admin.condition.report.*;
import com.group.admin.dto.res.report.*;
import com.group.admin.req.common.QueryReq;

import java.time.LocalDate;
import java.util.List;

/**
 * 報表服務介面
 */
public interface ReportService {
    
    /**
     * 營業額報表
     * @param req 查詢條件（包含店家ID、時間範圍等）
     */
    RevenueReportRes getRevenueReport(QueryReq<RevenueReportCondition> req);
    
    /**
     * 推薦碼報表
     * @param req 查詢條件
     */
    ReferralReportRes getReferralReport(QueryReq<ReferralReportCondition> req);
    
    /**
     * 開獎結果報表
     * @param req 查詢條件（包含店家ID、一番賞ID、時間範圍等）
     */
    LotteryResultReportRes getLotteryResultReport(QueryReq<LotteryResultReportCondition> req);
    
    /**
     * 儲值報表
     * @param req 查詢條件
     */
    RechargeReportRes getRechargeReport(QueryReq<RechargeReportCondition> req);
    
    /**
     * 贈送點數報表
     * @param req 查詢條件
     */
    BonusReportRes getBonusReport(QueryReq<BonusReportCondition> req);

    /**
     * 商品銷售排行報表
     * @param req 查詢條件（包含店家ID、回傳筆數、排序欄位）
     */
    LotterySalesRankingRes getLotterySalesRanking(QueryReq<LotterySalesRankingCondition> req);

    /**
     * 儲存報表快照
     */
    void saveReportSnapshot(String reportType, String periodType, String storeId, 
                            Object data, String summary);
    
    /**
     * 取得報表快照歷史
     */
    List<?> getReportSnapshots(String reportType, String periodType, String storeId);
}
