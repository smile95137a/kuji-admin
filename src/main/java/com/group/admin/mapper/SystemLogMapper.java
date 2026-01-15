package com.group.admin.mapper;

import com.group.admin.entity.SystemLog;
import com.group.admin.example.SystemLogExample;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface SystemLogMapper {
    long countByExample(SystemLogExample example);

    int deleteByExample(SystemLogExample example);

    int deleteByPrimaryKey(String id);

    int insert(SystemLog row);

    int insertSelective(SystemLog row);

    List<SystemLog> selectByExampleWithBLOBs(SystemLogExample example);

    List<SystemLog> selectByExample(SystemLogExample example);

    SystemLog selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") SystemLog row, @Param("example") SystemLogExample example);

    int updateByExampleWithBLOBs(@Param("row") SystemLog row, @Param("example") SystemLogExample example);

    int updateByExample(@Param("row") SystemLog row, @Param("example") SystemLogExample example);

    int updateByPrimaryKeySelective(SystemLog row);

    int updateByPrimaryKeyWithBLOBs(SystemLog row);

    int updateByPrimaryKey(SystemLog row);
    
    // ========== 自定義查詢方法（使用 Annotation）==========
    
    /**
     * 根據操作類型查詢日誌
     */
    @Select("SELECT * FROM system_log WHERE action = #{type} ORDER BY created_at DESC LIMIT #{limit}")
    List<SystemLog> selectByType(@Param("type") String type, @Param("limit") int limit);
    
    /**
     * 根據用戶 ID 查詢日誌
     */
    @Select("SELECT * FROM system_log WHERE user_id = #{userId} ORDER BY created_at DESC LIMIT #{limit}")
    List<SystemLog> selectByUserId(@Param("userId") String userId, @Param("limit") int limit);
    
    /**
     * 根據類型和時間範圍查詢
     */
    @Select("SELECT * FROM system_log " +
            "WHERE action = #{type} " +
            "AND created_at BETWEEN #{start} AND #{end} " +
            "ORDER BY created_at DESC")
    List<SystemLog> selectByTypeAndDateRange(@Param("type") String type, 
                                             @Param("start") LocalDateTime start, 
                                             @Param("end") LocalDateTime end);
    
    /**
     * 刪除舊日誌
     */
    @Delete("DELETE FROM system_log WHERE created_at < #{before}")
    int deleteOldLogs(@Param("before") LocalDateTime before);
}