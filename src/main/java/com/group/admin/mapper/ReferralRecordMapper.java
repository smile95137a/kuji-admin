package com.group.admin.mapper;

import com.group.admin.entity.ReferralRecord;
import com.group.admin.example.ReferralRecordExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface ReferralRecordMapper {
    long countByExample(ReferralRecordExample example);

    int deleteByExample(ReferralRecordExample example);

    int deleteByPrimaryKey(String id);

    int insert(ReferralRecord row);

    int insertSelective(ReferralRecord row);

    List<ReferralRecord> selectByExample(ReferralRecordExample example);

    ReferralRecord selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") ReferralRecord row, @Param("example") ReferralRecordExample example);

    int updateByExample(@Param("row") ReferralRecord row, @Param("example") ReferralRecordExample example);

    int updateByPrimaryKeySelective(ReferralRecord row);

    int updateByPrimaryKey(ReferralRecord row);
    
    // ========== 自定義查詢方法（使用 Annotation）==========
    
    /**
     * 根據用戶 ID 查詢使用記錄
     */
    @Select("SELECT * FROM referral_record WHERE user_id = #{userId} ORDER BY referred_at DESC")
    List<ReferralRecord> selectByUserId(@Param("userId") String userId);
    
    /**
     * 根據推薦碼 ID 查詢使用記錄
     */
    @Select("SELECT * FROM referral_record WHERE referral_code_id = #{codeId} ORDER BY referred_at DESC")
    List<ReferralRecord> selectByReferralCodeId(@Param("codeId") String codeId);
    
    /**
     * 根據店家 ID 查詢使用記錄
     */
    @Select("SELECT * FROM referral_record WHERE store_id = #{storeId} ORDER BY referred_at DESC")
    List<ReferralRecord> selectByStoreId(@Param("storeId") String storeId);
}