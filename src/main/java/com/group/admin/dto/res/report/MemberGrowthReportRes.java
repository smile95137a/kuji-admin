package com.group.admin.dto.res.report;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 會員成長報表回應
 */
@Data
@Builder
public class MemberGrowthReportRes {

    /** 實際查詢起始日 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /** 實際查詢結束日 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    // ── 新增會員統計 ──────────────────────────────

    /** 查詢期間新增會員總數（無資料時為 0） */
    private Integer totalNewMembers;

    /** 與上期相比成長率（%）；上期無資料時 null */
    private BigDecimal growthRate;

    /** 按 provider 分類，無資料時為空 Map {} */
    private Map<String, Integer> registrationByProvider;

    /** 每日明細，長度 = 查詢天數；無新增時 count = 0 */
    private List<DailyNewMember> dailyNewMembers;

    // ── 活躍度與 ARPU ─────────────────────────────

    /** 活躍會員數；無活躍時為 0 */
    private Integer activeMembers;

    /** 金幣 ARPU（精確到 0.1）；activeMembers = 0 時為 0.0 */
    private BigDecimal arpuGold;

    /** 紅利 ARPU（精確到 0.1）；activeMembers = 0 時為 0.0 */
    private BigDecimal arpuBonus;

    // ── 留存率 ────────────────────────────────────

    /** 7 天留存率（%）；前月無新增會員時 null */
    private BigDecimal retention7Days;

    /** 30 天留存率（%）；前月無新增會員時 null */
    private BigDecimal retention30Days;

    @Data
    @Builder
    public static class DailyNewMember {
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;
        private Integer count;
    }
}
