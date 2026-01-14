package com.group.admin.mapper;

import com.group.admin.entity.ReportSnapshot;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 報表快照 Mapper
 */
@Mapper
public interface ReportSnapshotMapper {
    
    @Insert("INSERT INTO report_snapshot (id, report_type, period_type, period_start, period_end, " +
            "store_id, data, summary, created_at) VALUES (#{id}, #{reportType}, #{periodType}, " +
            "#{periodStart}, #{periodEnd}, #{storeId}, #{data}, #{summary}, #{createdAt})")
    int insert(ReportSnapshot snapshot);
    
    @Select("SELECT * FROM report_snapshot WHERE id = #{id}")
    ReportSnapshot selectById(String id);
    
    @Select("SELECT * FROM report_snapshot WHERE report_type = #{reportType} AND period_type = #{periodType} " +
            "ORDER BY period_start DESC LIMIT #{limit}")
    List<ReportSnapshot> selectByTypeAndPeriod(@Param("reportType") String reportType,
                                                @Param("periodType") String periodType,
                                                @Param("limit") int limit);
    
    @Select("SELECT * FROM report_snapshot WHERE report_type = #{reportType} AND store_id = #{storeId} " +
            "ORDER BY period_start DESC LIMIT #{limit}")
    List<ReportSnapshot> selectByTypeAndStore(@Param("reportType") String reportType,
                                               @Param("storeId") String storeId,
                                               @Param("limit") int limit);
    
    @Select("SELECT * FROM report_snapshot WHERE report_type = #{reportType} " +
            "AND period_start >= #{startDate} AND period_end <= #{endDate} " +
            "ORDER BY period_start DESC")
    List<ReportSnapshot> selectByTypeAndDateRange(@Param("reportType") String reportType,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);
    
    @Select("SELECT * FROM report_snapshot WHERE report_type = #{reportType} AND period_type = #{periodType} " +
            "AND store_id = #{storeId} AND period_start = #{periodStart}")
    ReportSnapshot selectExisting(@Param("reportType") String reportType,
                                   @Param("periodType") String periodType,
                                   @Param("storeId") String storeId,
                                   @Param("periodStart") LocalDate periodStart);
    
    @Update("UPDATE report_snapshot SET data = #{data}, summary = #{summary} WHERE id = #{id}")
    int updateData(ReportSnapshot snapshot);
    
    @Delete("DELETE FROM report_snapshot WHERE created_at < #{before}")
    int deleteOldSnapshots(LocalDate before);
}
