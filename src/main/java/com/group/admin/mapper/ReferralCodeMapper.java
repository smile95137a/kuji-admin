package com.group.admin.mapper;

import com.group.admin.entity.ReferralCode;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 推薦碼 Mapper
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Mapper
public interface ReferralCodeMapper {
    
    @Insert("INSERT INTO referral_code (id, code, store_id, description, is_active, used_count, created_at, updated_at) " +
            "VALUES (#{id}, #{code}, #{storeId}, #{description}, #{isActive}, #{usedCount}, #{createdAt}, #{updatedAt})")
    int insert(ReferralCode record);
    
    @Select("SELECT * FROM referral_code WHERE id = #{id}")
    @Results({
        @Result(column = "id", property = "id"),
        @Result(column = "code", property = "code"),
        @Result(column = "store_id", property = "storeId"),
        @Result(column = "description", property = "description"),
        @Result(column = "is_active", property = "isActive"),
        @Result(column = "used_count", property = "usedCount"),
        @Result(column = "created_at", property = "createdAt"),
        @Result(column = "updated_at", property = "updatedAt")
    })
    ReferralCode selectByPrimaryKey(String id);
    
    @Select("SELECT * FROM referral_code WHERE code = #{code}")
    @Results({
        @Result(column = "id", property = "id"),
        @Result(column = "code", property = "code"),
        @Result(column = "store_id", property = "storeId"),
        @Result(column = "description", property = "description"),
        @Result(column = "is_active", property = "isActive"),
        @Result(column = "used_count", property = "usedCount"),
        @Result(column = "created_at", property = "createdAt"),
        @Result(column = "updated_at", property = "updatedAt")
    })
    ReferralCode selectByCode(String code);
    
    @Select("SELECT * FROM referral_code WHERE store_id = #{storeId}")
    @Results({
        @Result(column = "id", property = "id"),
        @Result(column = "code", property = "code"),
        @Result(column = "store_id", property = "storeId"),
        @Result(column = "description", property = "description"),
        @Result(column = "is_active", property = "isActive"),
        @Result(column = "used_count", property = "usedCount"),
        @Result(column = "created_at", property = "createdAt"),
        @Result(column = "updated_at", property = "updatedAt")
    })
    List<ReferralCode> selectByStoreId(String storeId);
    
    @Select("SELECT * FROM referral_code ORDER BY created_at DESC")
    @Results({
        @Result(column = "id", property = "id"),
        @Result(column = "code", property = "code"),
        @Result(column = "store_id", property = "storeId"),
        @Result(column = "description", property = "description"),
        @Result(column = "is_active", property = "isActive"),
        @Result(column = "used_count", property = "usedCount"),
        @Result(column = "created_at", property = "createdAt"),
        @Result(column = "updated_at", property = "updatedAt")
    })
    List<ReferralCode> selectAll();
    
    @Update("UPDATE referral_code SET code = #{code}, store_id = #{storeId}, description = #{description}, " +
            "is_active = #{isActive}, used_count = #{usedCount}, updated_at = #{updatedAt} WHERE id = #{id}")
    int updateByPrimaryKey(ReferralCode record);
    
    @Delete("DELETE FROM referral_code WHERE id = #{id}")
    int deleteByPrimaryKey(String id);
}
