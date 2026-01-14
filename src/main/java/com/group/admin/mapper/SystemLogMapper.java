package com.group.admin.mapper;

import com.group.admin.entity.SystemLog;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系統日誌 Mapper
 */
@Mapper
public interface SystemLogMapper {
    
    @Insert("INSERT INTO system_log (id, log_type, action, user_id, user_type, target_type, target_id, " +
            "request_ip, request_url, request_method, request_params, response_status, response_body, " +
            "error_message, error_stack, duration_ms, extra_data, created_at) " +
            "VALUES (#{id}, #{logType}, #{action}, #{userId}, #{userType}, #{targetType}, #{targetId}, " +
            "#{requestIp}, #{requestUrl}, #{requestMethod}, #{requestParams}, #{responseStatus}, #{responseBody}, " +
            "#{errorMessage}, #{errorStack}, #{durationMs}, #{extraData}, #{createdAt})")
    int insert(SystemLog log);
    
    @Select("SELECT * FROM system_log WHERE id = #{id}")
    SystemLog selectById(String id);
    
    @Select("SELECT * FROM system_log WHERE log_type = #{logType} ORDER BY created_at DESC LIMIT #{limit}")
    List<SystemLog> selectByType(@Param("logType") String logType, @Param("limit") int limit);
    
    @Select("SELECT * FROM system_log WHERE user_id = #{userId} ORDER BY created_at DESC LIMIT #{limit}")
    List<SystemLog> selectByUserId(@Param("userId") String userId, @Param("limit") int limit);
    
    @Select("SELECT * FROM system_log WHERE log_type = #{logType} AND created_at BETWEEN #{start} AND #{end} " +
            "ORDER BY created_at DESC")
    List<SystemLog> selectByTypeAndDateRange(@Param("logType") String logType, 
                                              @Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end);
    
    @Select("SELECT * FROM system_log WHERE action = #{action} ORDER BY created_at DESC LIMIT #{limit}")
    List<SystemLog> selectByAction(@Param("action") String action, @Param("limit") int limit);
    
    @Select("SELECT * FROM system_log ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<SystemLog> selectWithPagination(@Param("limit") int limit, @Param("offset") int offset);
    
    @Select("SELECT COUNT(*) FROM system_log WHERE log_type = #{logType}")
    long countByType(String logType);
    
    @Delete("DELETE FROM system_log WHERE created_at < #{before}")
    int deleteOldLogs(LocalDateTime before);
}
