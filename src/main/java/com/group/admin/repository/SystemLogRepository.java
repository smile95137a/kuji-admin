package com.group.admin.repository;

import com.group.admin.entity.SystemLog;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系統日誌自定義查詢 Repository
 * 這個檔案不會被 MyBatis Generator 覆蓋
 */
@Mapper
public interface SystemLogRepository {
    
    @Select("SELECT * FROM system_log WHERE log_type = #{logType} ORDER BY created_at DESC LIMIT #{limit}")
    List<SystemLog> selectByType(@Param("logType") String logType, @Param("limit") int limit);
    
    @Select("SELECT * FROM system_log WHERE user_id = #{userId} ORDER BY created_at DESC LIMIT #{limit}")
    List<SystemLog> selectByUserId(@Param("userId") String userId, @Param("limit") int limit);
    
    @Select("SELECT * FROM system_log WHERE created_at BETWEEN #{startTime} AND #{endTime} ORDER BY created_at DESC")
    List<SystemLog> selectByTypeAndDateRange(@Param("logType") String logType, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    @Delete("DELETE FROM system_log WHERE created_at < #{beforeDate}")
    int deleteOldLogs(@Param("beforeDate") LocalDateTime beforeDate);
}
