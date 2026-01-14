package com.group.admin.service;

import com.group.admin.dto.res.report.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 報表服務介面
 */
public interface ReportService {
    
    /**
     * 營業額報表
     * @param storeId 店家ID（可選，null 表示全部）
     * @param startDate 開始日期
     * @param endDate 結束日期
     */
    RevenueReportRes getRevenueReport(String storeId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 推薦碼報表
     * @param storeId 店家ID（可選）
     * @param startDate 開始日期
     * @param endDate 結束日期
     */
    ReferralReportRes getReferralReport(String storeId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 開獎結果報表
     * @param storeId 店家ID（可選）
     * @param lotteryId 一番賞ID（可選）
     * @param startDate 開始日期
     * @param endDate 結束日期
     */
    LotteryResultReportRes getLotteryResultReport(String storeId, String lotteryId, 
                                                   LocalDate startDate, LocalDate endDate);
    
    /**
     * 儲值報表
     * @param storeId 店家ID（可選）
     * @param startDate 開始日期
     * @param endDate 結束日期
     */
    RechargeReportRes getRechargeReport(String storeId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 贈送點數報表
     * @param storeId 店家ID（可選）
     * @param startDate 開始日期
     * @param endDate 結束日期
     */
    BonusReportRes getBonusReport(String storeId, LocalDate startDate, LocalDate endDate);
    
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
