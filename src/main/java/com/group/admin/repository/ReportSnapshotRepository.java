package com.group.admin.repository;

import com.group.admin.entity.ReportSnapshot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 報表快照自定義查詢 Repository
 * 這個檔案不會被 MyBatis Generator 覆蓋
 */
@Mapper
public interface ReportSnapshotRepository {
    
    @Select("SELECT * FROM report_snapshot WHERE report_type = #{reportType} AND period = #{period} ORDER BY created_at DESC LIMIT #{limit}")
    List<ReportSnapshot> selectByTypeAndPeriod(@Param("reportType") String reportType, @Param("period") String period, @Param("limit") int limit);
}
