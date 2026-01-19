package com.group.admin.repository;

import com.group.admin.entity.ReferralCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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
}
