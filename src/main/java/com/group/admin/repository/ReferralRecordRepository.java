package com.group.admin.repository;

import com.group.admin.entity.ReferralRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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
}
