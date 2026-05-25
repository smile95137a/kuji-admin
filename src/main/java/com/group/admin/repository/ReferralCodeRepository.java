package com.group.admin.repository;

import com.group.admin.condition.report.ReferralReportCondition;
import com.group.admin.entity.ReferralCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 推薦碼自定義查詢 Repository
 * 這個檔案不會被 MyBatis Generator 覆蓋
 */
@Mapper
public interface ReferralCodeRepository {

    @Select("SELECT * FROM referral_code WHERE code = #{code}")
    ReferralCode selectByCode(@Param("code") String code);

    @Select("SELECT * FROM referral_code WHERE store_id = #{storeId} ORDER BY created_at DESC")
    List<ReferralCode> selectByStoreId(@Param("storeId") String storeId);

    @Select("SELECT * FROM referral_code ORDER BY created_at DESC")
    List<ReferralCode> selectAll();

    /**
     * T016: Per-store totals + active code count (with optional filters)
     */
    @Select("""
            <script>
            SELECT
                s.id        AS storeId,
                s.store_name AS storeName,
                COUNT(DISTINCT rr.id) AS totalReferrals,
                COUNT(DISTINCT CASE WHEN rc.is_active = 1 THEN rc.id END) AS activeCodeCount
            FROM store s
            LEFT JOIN referral_code rc ON rc.store_id = s.id
            LEFT JOIN referral_record rr ON rr.referral_code_id = rc.id
                <if test="startDate != null and endDate != null">
                AND rr.referred_at >= #{startDate}
                AND rr.referred_at &lt; DATE_ADD(#{endDate}, INTERVAL 1 DAY)
                </if>
            WHERE s.status = 'ACTIVE'
            <if test="storeId != null and storeId != ''">
              AND s.id = #{storeId}
            </if>
            GROUP BY s.id, s.store_name
            </script>
            """)
    List<Map<String, Object>> selectStatsByStore(@Param("storeId") String storeId,
                                                  @Param("startDate") Object startDate,
                                                  @Param("endDate") Object endDate);

    default List<Map<String, Object>> selectStatsByStore(ReferralReportCondition condition) {
        String sid = condition != null ? condition.getStoreId() : null;
        Object start = condition != null ? condition.getStartDate() : null;
        Object end = condition != null ? condition.getEndDate() : null;
        return selectStatsByStore(sid, start, end);
    }
}
