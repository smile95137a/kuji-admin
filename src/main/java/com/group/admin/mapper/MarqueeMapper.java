package com.group.admin.mapper;

import com.group.admin.entity.Marquee;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 跑馬燈 Mapper
 */
@Mapper
public interface MarqueeMapper {
    
    @Insert("INSERT INTO marquee (id, content, link_url, link_type, priority, bg_color, text_color, " +
            "start_time, end_time, is_active, created_by, created_at, updated_at) " +
            "VALUES (#{id}, #{content}, #{linkUrl}, #{linkType}, #{priority}, #{bgColor}, #{textColor}, " +
            "#{startTime}, #{endTime}, #{isActive}, #{createdBy}, #{createdAt}, #{updatedAt})")
    int insert(Marquee marquee);
    
    @Update("UPDATE marquee SET content = #{content}, link_url = #{linkUrl}, link_type = #{linkType}, " +
            "priority = #{priority}, bg_color = #{bgColor}, text_color = #{textColor}, " +
            "start_time = #{startTime}, end_time = #{endTime}, is_active = #{isActive}, " +
            "updated_at = #{updatedAt} WHERE id = #{id}")
    int update(Marquee marquee);
    
    @Delete("DELETE FROM marquee WHERE id = #{id}")
    int deleteById(String id);
    
    @Select("SELECT * FROM marquee WHERE id = #{id}")
    Marquee selectById(String id);
    
    @Select("SELECT * FROM marquee ORDER BY priority DESC, created_at DESC")
    List<Marquee> selectAll();
    
    @Select("SELECT * FROM marquee WHERE is_active = 1 " +
            "AND (start_time IS NULL OR start_time <= #{now}) " +
            "AND (end_time IS NULL OR end_time >= #{now}) " +
            "ORDER BY priority DESC, created_at DESC")
    List<Marquee> selectActiveMarquees(LocalDateTime now);
    
    @Update("UPDATE marquee SET is_active = #{isActive}, updated_at = #{updatedAt} WHERE id = #{id}")
    int updateStatus(@Param("id") String id, @Param("isActive") Byte isActive, 
                     @Param("updatedAt") LocalDateTime updatedAt);
}
