package com.group.admin.mapper;

import com.group.admin.entity.ReportSnapshot;
import com.group.admin.example.ReportSnapshotExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface ReportSnapshotMapper {
    long countByExample(ReportSnapshotExample example);

    int deleteByExample(ReportSnapshotExample example);

    int deleteByPrimaryKey(String id);

    int insert(ReportSnapshot row);

    int insertSelective(ReportSnapshot row);

    List<ReportSnapshot> selectByExampleWithBLOBs(ReportSnapshotExample example);

    List<ReportSnapshot> selectByExample(ReportSnapshotExample example);

    ReportSnapshot selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") ReportSnapshot row, @Param("example") ReportSnapshotExample example);

    int updateByExampleWithBLOBs(@Param("row") ReportSnapshot row, @Param("example") ReportSnapshotExample example);

    int updateByExample(@Param("row") ReportSnapshot row, @Param("example") ReportSnapshotExample example);

    int updateByPrimaryKeySelective(ReportSnapshot row);

    int updateByPrimaryKeyWithBLOBs(ReportSnapshot row);

    int updateByPrimaryKey(ReportSnapshot row);
    
    // ========== 自定義查詢方法（使用 Annotation）==========
    
    /**
     * 根據類型和週期查詢報表快照
     */
    @Select("SELECT * FROM report_snapshot " +
            "WHERE report_type = #{type} AND period = #{period} " +
            "ORDER BY created_at DESC LIMIT #{limit}")
    List<ReportSnapshot> selectByTypeAndPeriod(@Param("type") String type, 
                                               @Param("period") String period, 
                                               @Param("limit") int limit);
}