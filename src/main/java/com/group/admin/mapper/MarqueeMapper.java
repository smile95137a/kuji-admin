package com.group.admin.mapper;

import com.group.admin.entity.Marquee;
import com.group.admin.example.MarqueeExample;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface MarqueeMapper {
    long countByExample(MarqueeExample example);

    int deleteByExample(MarqueeExample example);

    int deleteByPrimaryKey(String id);

    int insert(Marquee row);

    int insertSelective(Marquee row);

    List<Marquee> selectByExample(MarqueeExample example);

    Marquee selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") Marquee row, @Param("example") MarqueeExample example);

    int updateByExample(@Param("row") Marquee row, @Param("example") MarqueeExample example);

    int updateByPrimaryKeySelective(Marquee row);

    int updateByPrimaryKey(Marquee row);
    
    // ========== 自定義查詢方法（使用 Annotation）==========
    
    /**
     * 查詢當前啟用的跑馬燈
     */
    @Select("SELECT * FROM marquee " +
            "WHERE is_enabled = 1 " +
            "AND (start_time IS NULL OR start_time <= #{now}) " +
            "AND (end_time IS NULL OR end_time >= #{now}) " +
            "ORDER BY order_num ASC")
    List<Marquee> selectActiveMarquees(@Param("now") LocalDateTime now);
    
    /**
     * 查詢所有跑馬燈
     */
    @Select("SELECT * FROM marquee ORDER BY order_num ASC")
    List<Marquee> selectAll();
    
    /**
     * 根據 ID 查詢
     */
    @Select("SELECT * FROM marquee WHERE id = #{id}")
    Marquee selectById(@Param("id") String id);
    
    /**
     * 更新跑馬燈
     */
    @Update("UPDATE marquee SET " +
            "content = #{content}, " +
            "link_url = #{linkUrl}, " +
            "order_num = #{orderNum}, " +
            "is_enabled = #{isEnabled}, " +
            "start_time = #{startTime}, " +
            "end_time = #{endTime}, " +
            "updated_at = #{updatedAt} " +
            "WHERE id = #{id}")
    int update(Marquee marquee);
    
    /**
     * 刪除跑馬燈
     */
    @Delete("DELETE FROM marquee WHERE id = #{id}")
    int deleteById(@Param("id") String id);
    
    /**
     * 更新啟用狀態
     */
    @Update("UPDATE marquee SET is_enabled = #{enabled}, updated_at = #{updatedAt} WHERE id = #{id}")
    int updateStatus(@Param("id") String id, @Param("enabled") Byte enabled, @Param("updatedAt") LocalDateTime updatedAt);
}