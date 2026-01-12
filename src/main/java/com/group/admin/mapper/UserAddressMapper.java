package com.group.admin.mapper;

import com.group.admin.entity.UserAddress;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 會員地址 Mapper
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Mapper
public interface UserAddressMapper {
    
    @Insert("INSERT INTO user_address (id, user_id, label, recipient_name, recipient_phone, city, district, zip_code, address, is_default, created_at, updated_at) " +
            "VALUES (#{id}, #{userId}, #{label}, #{recipientName}, #{recipientPhone}, #{city}, #{district}, #{zipCode}, #{address}, #{isDefault}, #{createdAt}, #{updatedAt})")
    int insert(UserAddress record);
    
    @Select("SELECT * FROM user_address WHERE id = #{id}")
    @Results({
        @Result(column = "id", property = "id"),
        @Result(column = "user_id", property = "userId"),
        @Result(column = "label", property = "label"),
        @Result(column = "recipient_name", property = "recipientName"),
        @Result(column = "recipient_phone", property = "recipientPhone"),
        @Result(column = "city", property = "city"),
        @Result(column = "district", property = "district"),
        @Result(column = "zip_code", property = "zipCode"),
        @Result(column = "address", property = "address"),
        @Result(column = "is_default", property = "isDefault"),
        @Result(column = "created_at", property = "createdAt"),
        @Result(column = "updated_at", property = "updatedAt")
    })
    UserAddress selectByPrimaryKey(String id);
    
    @Select("SELECT * FROM user_address WHERE user_id = #{userId} ORDER BY is_default DESC, created_at DESC")
    @Results({
        @Result(column = "id", property = "id"),
        @Result(column = "user_id", property = "userId"),
        @Result(column = "label", property = "label"),
        @Result(column = "recipient_name", property = "recipientName"),
        @Result(column = "recipient_phone", property = "recipientPhone"),
        @Result(column = "city", property = "city"),
        @Result(column = "district", property = "district"),
        @Result(column = "zip_code", property = "zipCode"),
        @Result(column = "address", property = "address"),
        @Result(column = "is_default", property = "isDefault"),
        @Result(column = "created_at", property = "createdAt"),
        @Result(column = "updated_at", property = "updatedAt")
    })
    List<UserAddress> selectByUserId(String userId);
    
    @Select("SELECT * FROM user_address WHERE user_id = #{userId} AND is_default = 1 LIMIT 1")
    @Results({
        @Result(column = "id", property = "id"),
        @Result(column = "user_id", property = "userId"),
        @Result(column = "label", property = "label"),
        @Result(column = "recipient_name", property = "recipientName"),
        @Result(column = "recipient_phone", property = "recipientPhone"),
        @Result(column = "city", property = "city"),
        @Result(column = "district", property = "district"),
        @Result(column = "zip_code", property = "zipCode"),
        @Result(column = "address", property = "address"),
        @Result(column = "is_default", property = "isDefault"),
        @Result(column = "created_at", property = "createdAt"),
        @Result(column = "updated_at", property = "updatedAt")
    })
    UserAddress selectDefaultByUserId(String userId);
    
    @Update("UPDATE user_address SET label = #{label}, recipient_name = #{recipientName}, recipient_phone = #{recipientPhone}, " +
            "city = #{city}, district = #{district}, zip_code = #{zipCode}, address = #{address}, " +
            "is_default = #{isDefault}, updated_at = #{updatedAt} WHERE id = #{id}")
    int updateByPrimaryKey(UserAddress record);
    
    @Update("UPDATE user_address SET is_default = 0 WHERE user_id = #{userId}")
    int clearDefaultByUserId(String userId);
    
    @Delete("DELETE FROM user_address WHERE id = #{id}")
    int deleteByPrimaryKey(String id);
}
