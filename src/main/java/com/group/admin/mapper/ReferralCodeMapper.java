package com.group.admin.mapper;

import com.group.admin.entity.ReferralCode;
import com.group.admin.example.ReferralCodeExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface ReferralCodeMapper {
    long countByExample(ReferralCodeExample example);

    int deleteByExample(ReferralCodeExample example);

    int deleteByPrimaryKey(String id);

    int insert(ReferralCode row);

    int insertSelective(ReferralCode row);

    List<ReferralCode> selectByExample(ReferralCodeExample example);

    ReferralCode selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") ReferralCode row, @Param("example") ReferralCodeExample example);

    int updateByExample(@Param("row") ReferralCode row, @Param("example") ReferralCodeExample example);

    int updateByPrimaryKeySelective(ReferralCode row);

    int updateByPrimaryKey(ReferralCode row);
    
    // ========== 自定義查詢方法（使用 Annotation）==========
    
    /**
     * 根據推薦碼查詢
     */
    @Select("SELECT * FROM referral_code WHERE code = #{code}")
    ReferralCode selectByCode(@Param("code") String code);
    
    /**
     * 根據店家 ID 查詢
     */
    @Select("SELECT * FROM referral_code WHERE store_id = #{storeId} ORDER BY created_at DESC")
    List<ReferralCode> selectByStoreId(@Param("storeId") String storeId);
    
    /**
     * 查詢所有推薦碼
     */
    @Select("SELECT * FROM referral_code ORDER BY created_at DESC")
    List<ReferralCode> selectAll();
}