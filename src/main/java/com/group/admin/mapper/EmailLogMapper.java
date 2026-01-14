package com.group.admin.mapper;

import com.group.admin.entity.EmailLog;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 郵件日誌 Mapper
 */
@Mapper
public interface EmailLogMapper {
    
    @Insert("INSERT INTO email_log (id, email_type, to_email, to_name, subject, content, template_name, " +
            "template_params, status, error_message, sent_at, retry_count, related_type, related_id, " +
            "created_at, updated_at) VALUES (#{id}, #{emailType}, #{toEmail}, #{toName}, #{subject}, " +
            "#{content}, #{templateName}, #{templateParams}, #{status}, #{errorMessage}, #{sentAt}, " +
            "#{retryCount}, #{relatedType}, #{relatedId}, #{createdAt}, #{updatedAt})")
    int insert(EmailLog log);
    
    @Update("UPDATE email_log SET status = #{status}, sent_at = #{sentAt}, error_message = #{errorMessage}, " +
            "retry_count = #{retryCount}, updated_at = #{updatedAt} WHERE id = #{id}")
    int updateStatus(EmailLog log);
    
    @Select("SELECT * FROM email_log WHERE id = #{id}")
    EmailLog selectById(String id);
    
    @Select("SELECT * FROM email_log WHERE to_email = #{toEmail} ORDER BY created_at DESC LIMIT #{limit}")
    List<EmailLog> selectByEmail(@Param("toEmail") String toEmail, @Param("limit") int limit);
    
    @Select("SELECT * FROM email_log WHERE email_type = #{emailType} ORDER BY created_at DESC LIMIT #{limit}")
    List<EmailLog> selectByType(@Param("emailType") String emailType, @Param("limit") int limit);
    
    @Select("SELECT * FROM email_log WHERE status = #{status} AND retry_count < #{maxRetry} " +
            "ORDER BY created_at ASC LIMIT #{limit}")
    List<EmailLog> selectPendingForRetry(@Param("status") String status, 
                                          @Param("maxRetry") int maxRetry,
                                          @Param("limit") int limit);
    
    @Select("SELECT * FROM email_log WHERE created_at BETWEEN #{start} AND #{end} ORDER BY created_at DESC")
    List<EmailLog> selectByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Select("SELECT COUNT(*) FROM email_log WHERE email_type = #{emailType} AND status = #{status}")
    long countByTypeAndStatus(@Param("emailType") String emailType, @Param("status") String status);
}
