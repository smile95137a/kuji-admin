package com.group.admin.mapper;

import com.group.admin.entity.UserAddress;
import com.group.admin.example.UserAddressExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface UserAddressMapper {
    long countByExample(UserAddressExample example);

    int deleteByExample(UserAddressExample example);

    int deleteByPrimaryKey(String id);

    int insert(UserAddress row);

    int insertSelective(UserAddress row);

    List<UserAddress> selectByExample(UserAddressExample example);

    UserAddress selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") UserAddress row, @Param("example") UserAddressExample example);

    int updateByExample(@Param("row") UserAddress row, @Param("example") UserAddressExample example);

    int updateByPrimaryKeySelective(UserAddress row);

    int updateByPrimaryKey(UserAddress row);
    
    // ========== 自定義查詢方法（使用 Annotation）==========
    
    /**
     * 清除用戶的所有預設地址
     */
    @Update("UPDATE user_address SET is_default = 0 WHERE user_id = #{userId}")
    int clearDefaultByUserId(@Param("userId") String userId);
    
    /**
     * 根據用戶 ID 查詢所有地址
     */
    @Select("SELECT * FROM user_address WHERE user_id = #{userId} ORDER BY is_default DESC, created_at DESC")
    List<UserAddress> selectByUserId(@Param("userId") String userId);
    
    /**
     * 查詢用戶的預設地址
     */
    @Select("SELECT * FROM user_address WHERE user_id = #{userId} AND is_default = 1 LIMIT 1")
    UserAddress selectDefaultByUserId(@Param("userId") String userId);
}