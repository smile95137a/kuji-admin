package com.group.admin.repository;

import com.group.admin.entity.ConsumptionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 消費紀錄自定義查詢 Repository
 * 這個檔案不會被 MyBatis Generator 覆蓋
 */
@Mapper
public interface ConsumptionRecordRepository {

    @Select("SELECT * FROM consumption_record WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<ConsumptionRecord> selectByUserId(@Param("userId") String userId);

    @Select("SELECT * FROM consumption_record ORDER BY created_at DESC")
    List<ConsumptionRecord> selectAll();
}
