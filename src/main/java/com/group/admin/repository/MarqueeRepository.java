package com.group.admin.repository;

import com.group.admin.entity.Marquee;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 跑馬燈自定義查詢 Repository
 * 這個檔案不會被 MyBatis Generator 覆蓋
 */
@Mapper
public interface MarqueeRepository {
    
    @Select("SELECT * FROM marquee WHERE is_active = 1 AND (start_time IS NULL OR start_time <= #{now}) AND (end_time IS NULL OR end_time >= #{now}) ORDER BY priority DESC, created_at DESC")
    List<Marquee> selectActiveMarquees(@Param("now") LocalDateTime now);
    
    @Select("SELECT * FROM marquee ORDER BY created_at DESC")
    List<Marquee> selectAll();
    
    @Select("SELECT * FROM marquee WHERE id = #{id}")
    Marquee selectById(@Param("id") String id);
    
    @Update("UPDATE marquee SET content = #{content}, link_url = #{linkUrl}, priority = #{priority}, is_active = #{isActive}, start_time = #{startTime}, end_time = #{endTime}, updated_at = #{updatedAt} WHERE id = #{id}")
    int update(Marquee marquee);
    
    @Delete("DELETE FROM marquee WHERE id = #{id}")
    int deleteById(@Param("id") String id);
    
    @Update("UPDATE marquee SET is_active = #{isActive}, updated_at = #{updatedAt} WHERE id = #{id}")
    int updateStatus(@Param("id") String id, @Param("isActive") Byte isActive, @Param("updatedAt") LocalDateTime updatedAt);
}
