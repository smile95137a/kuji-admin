package com.group.admin.mapper;

import com.group.admin.entity.ReferralRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 推薦關係紀錄 Mapper
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Mapper
public interface ReferralRecordMapper {
    
    @Insert("INSERT INTO referral_record (id, user_id, referral_code_id, store_id, used_code, referred_at) " +
            "VALUES (#{id}, #{userId}, #{referralCodeId}, #{storeId}, #{usedCode}, #{referredAt})")
    int insert(ReferralRecord record);
    
    @Select("SELECT * FROM referral_record WHERE id = #{id}")
    @Results({
        @Result(column = "id", property = "id"),
        @Result(column = "user_id", property = "userId"),
        @Result(column = "referral_code_id", property = "referralCodeId"),
        @Result(column = "store_id", property = "storeId"),
        @Result(column = "used_code", property = "usedCode"),
        @Result(column = "referred_at", property = "referredAt")
    })
    ReferralRecord selectByPrimaryKey(String id);
    
    @Select("SELECT * FROM referral_record WHERE user_id = #{userId}")
    @Results({
        @Result(column = "id", property = "id"),
        @Result(column = "user_id", property = "userId"),
        @Result(column = "referral_code_id", property = "referralCodeId"),
        @Result(column = "store_id", property = "storeId"),
        @Result(column = "used_code", property = "usedCode"),
        @Result(column = "referred_at", property = "referredAt")
    })
    ReferralRecord selectByUserId(String userId);
    
    @Select("SELECT * FROM referral_record WHERE store_id = #{storeId} ORDER BY referred_at DESC")
    @Results({
        @Result(column = "id", property = "id"),
        @Result(column = "user_id", property = "userId"),
        @Result(column = "referral_code_id", property = "referralCodeId"),
        @Result(column = "store_id", property = "storeId"),
        @Result(column = "used_code", property = "usedCode"),
        @Result(column = "referred_at", property = "referredAt")
    })
    List<ReferralRecord> selectByStoreId(String storeId);
    
    @Select("SELECT * FROM referral_record WHERE referral_code_id = #{referralCodeId} ORDER BY referred_at DESC")
    @Results({
        @Result(column = "id", property = "id"),
        @Result(column = "user_id", property = "userId"),
        @Result(column = "referral_code_id", property = "referralCodeId"),
        @Result(column = "store_id", property = "storeId"),
        @Result(column = "used_code", property = "usedCode"),
        @Result(column = "referred_at", property = "referredAt")
    })
    List<ReferralRecord> selectByReferralCodeId(String referralCodeId);
    
    @Select("SELECT COUNT(*) FROM referral_record WHERE store_id = #{storeId}")
    int countByStoreId(String storeId);
    
    @Select("SELECT COUNT(*) FROM referral_record WHERE referral_code_id = #{referralCodeId}")
    int countByReferralCodeId(String referralCodeId);
}
