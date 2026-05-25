package com.group.admin.repository;

import com.group.admin.condition.report.ReferralReportCondition;
import com.group.admin.entity.ReferralRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 推薦記錄自定義查詢 Repository
 * 這個檔案不會被 MyBatis Generator 覆蓋
 */
@Mapper
public interface ReferralRecordRepository {

    @Select("SELECT * FROM referral_record WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<ReferralRecord> selectByUserId(@Param("userId") String userId);

    @Select("SELECT * FROM referral_record WHERE referral_code_id = #{referralCodeId} ORDER BY created_at DESC")
    List<ReferralRecord> selectByReferralCodeId(@Param("referralCodeId") String referralCodeId);

    @Select("SELECT * FROM referral_record WHERE store_id = #{storeId} ORDER BY created_at DESC")
    List<ReferralRecord> selectByStoreId(@Param("storeId") String storeId);

    /**
     * T017: Daily timeline aggregation per store (with optional filters)
     */
    @Select("""
            <script>
            SELECT
                rr.store_id AS storeId,
                DATE(rr.referred_at) AS referralDate,
                COUNT(*) AS dailyCount
            FROM referral_record rr
            WHERE 1 = 1
            <if test="startDate != null and endDate != null">
              AND rr.referred_at >= #{startDate}
              AND rr.referred_at &lt; DATE_ADD(#{endDate}, INTERVAL 1 DAY)
            </if>
            <if test="storeId != null and storeId != ''">
              AND rr.store_id = #{storeId}
            </if>
            GROUP BY rr.store_id, DATE(rr.referred_at)
            ORDER BY referralDate ASC
            </script>
            """)
    List<Map<String, Object>> selectTimelineByStore(@Param("storeId") String storeId,
                                                     @Param("startDate") Object startDate,
                                                     @Param("endDate") Object endDate);

    default List<Map<String, Object>> selectTimelineByStore(ReferralReportCondition condition) {
        String sid = condition != null ? condition.getStoreId() : null;
        Object start = condition != null ? condition.getStartDate() : null;
        Object end = condition != null ? condition.getEndDate() : null;
        return selectTimelineByStore(sid, start, end);
    }
}
