package com.group.admin.dto.res.report;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 店家績效比較報表回應
 */
@Data
@Builder
public class StorePerformanceReportRes {

    /**
     * 報表期間起
     */
    private LocalDate startDate;

    /**
     * 報表期間迄
     */
    private LocalDate endDate;

    /**
     * 各店家績效清單
     */
    private List<StoreItem> stores;

    /**
     * 單店每日趨勢（僅帶入 storeId 時回傳；否則為 null）
     */
    private List<DailyStat> dailyStats;

    /**
     * 單一店家績效資料
     */
    @Data
    @Builder
    public static class StoreItem {
        /** 店家 UUID */
        private String storeId;
        /** 店家名稱 */
        private String storeName;
        /** 抽獎消費總點數（ABS of wallet_transaction DRAW） */
        private Long totalRevenue;
        /** 抽籤總數（lottery_ticket status=DRAWN） */
        private Integer drawCount;
        /** 不重複活躍用戶數 */
        private Integer activeUsers;
        /** 出貨率 %；分母為 0 時 null */
        private Double shipRate;
        /** 逾期率 %；無訂單時 null */
        private Double overdueRate;
        /** 平均出貨天數；029 未合併前恆為 null */
        private Double avgShipDays;
    }

    /**
     * 每日統計資料
     */
    @Data
    @Builder
    public static class DailyStat {
        /** 日期 */
        private LocalDate date;
        /** 當日抽籤數 */
        private Integer drawCount;
        /** 當日抽獎消費點數 */
        private Long revenue;
        /** 當日新用戶數（首次在本店有活動） */
        private Integer newUsers;
    }
}
