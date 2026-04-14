package com.group.admin.res.referral;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 管理員推薦碼統計回應（店家層級聚合）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminReferralStatsRes {

    private String storeId;
    private String storeName;
    private Long totalReferrals;
    private Long activeCodeCount;
    private List<DailyCount> timeline;

    @Data
    @AllArgsConstructor
    public static class DailyCount {
        private String date;
        private Long count;
    }
}
