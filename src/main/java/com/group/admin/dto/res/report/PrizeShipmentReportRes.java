package com.group.admin.dto.res.report;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 獎品出貨報表回應 DTO
 */
@Data
@Builder
public class PrizeShipmentReportRes {

    /** 查詢開始日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /** 查詢結束日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    // === 狀態計數（排除 CANCELLED、PAYMENT_PENDING） ===

    /** PENDING 狀態訂單數 */
    private Integer pendingCount;

    /** PREPARING 狀態訂單數 */
    private Integer preparingCount;

    /** SHIPPED 狀態訂單數 */
    private Integer shippedCount;

    /** COMPLETED 狀態訂單數 */
    private Integer completedCount;

    // === 時效指標 ===

    /**
     * 平均出貨天數（preparing_at → shipped_at），精確至 0.1 天；
     * 無資料（或所有訂單 preparing_at 為 null）時為 null
     */
    private BigDecimal avgShipDays;

    /** 超過 7 天仍在 PENDING 狀態的訂單筆數（不受日期範圍限制，反映即時狀態） */
    private Integer overdueCount;

    // === 每日出貨明細（按 shipped_at 日期分組） ===
    private List<DailyShipment> dailyDetails;

    // === 跨店家統計（Admin 限定；StoreOwner 查詢時為 null） ===
    private List<StoreShipment> storeDetails;

    // ---- Inner classes ----

    /**
     * 每日出貨明細
     */
    @Data
    @Builder
    public static class DailyShipment {
        /** 出貨日期（shipped_at 的日期部分） */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;
        /** 當日出貨筆數（SHIPPED + COMPLETED 狀態） */
        private Integer shippedCount;
    }

    /**
     * 跨店家統計（Admin 限定）
     */
    @Data
    @Builder
    public static class StoreShipment {
        private String storeId;
        private String storeName;
        private Integer pendingCount;
        private Integer preparingCount;
        private Integer shippedCount;
        private Integer completedCount;
        /** 該店平均出貨天數 */
        private BigDecimal avgShipDays;
        /** 該店逾期未備貨訂單數 */
        private Integer overdueCount;
    }
}
