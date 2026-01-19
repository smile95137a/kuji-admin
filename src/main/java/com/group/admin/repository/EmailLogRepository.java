package com.group.admin.repository;

import com.group.admin.entity.EmailLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 郵件日誌自定義查詢 Repository
 * 這個檔案不會被 MyBatis Generator 覆蓋
 */
@Mapper
public interface EmailLogRepository {
    
    @Select("SELECT * FROM email_log WHERE status = #{status} AND retry_count < #{maxRetries} ORDER BY created_at ASC LIMIT #{limit}")
    List<EmailLog> selectPendingForRetry(@Param("status") String status, @Param("maxRetries") int maxRetries, @Param("limit") int limit);
    
    @Update("UPDATE email_log SET status = #{status}, sent_at = #{sentAt}, retry_count = #{retryCount}, error_message = #{errorMessage}, updated_at = #{updatedAt} WHERE id = #{id}")
    int updateStatus(EmailLog emailLog);
}
