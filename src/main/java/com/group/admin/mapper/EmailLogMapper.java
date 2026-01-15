package com.group.admin.mapper;

import com.group.admin.entity.EmailLog;
import com.group.admin.example.EmailLogExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface EmailLogMapper {
    long countByExample(EmailLogExample example);

    int deleteByExample(EmailLogExample example);

    int deleteByPrimaryKey(String id);

    int insert(EmailLog row);

    int insertSelective(EmailLog row);

    List<EmailLog> selectByExampleWithBLOBs(EmailLogExample example);

    List<EmailLog> selectByExample(EmailLogExample example);

    EmailLog selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") EmailLog row, @Param("example") EmailLogExample example);

    int updateByExampleWithBLOBs(@Param("row") EmailLog row, @Param("example") EmailLogExample example);

    int updateByExample(@Param("row") EmailLog row, @Param("example") EmailLogExample example);

    int updateByPrimaryKeySelective(EmailLog row);

    int updateByPrimaryKeyWithBLOBs(EmailLog row);

    int updateByPrimaryKey(EmailLog row);
    
    // ========== 自定義查詢方法（使用 Annotation）==========
    
    /**
     * 查詢待重試的失敗郵件
     */
    @Select("SELECT * FROM email_log " +
            "WHERE status = #{status} AND retry_count < #{maxRetries} " +
            "ORDER BY created_at ASC LIMIT #{limit}")
    List<EmailLog> selectPendingForRetry(@Param("status") String status, 
                                         @Param("maxRetries") int maxRetries, 
                                         @Param("limit") int limit);
    
    /**
     * 更新郵件狀態
     */
    @Update("UPDATE email_log SET " +
            "status = #{status}, " +
            "sent_at = #{sentAt}, " +
            "retry_count = #{retryCount}, " +
            "error_message = #{errorMessage}, " +
            "updated_at = #{updatedAt} " +
            "WHERE id = #{id}")
    int updateStatus(EmailLog emailLog);
}