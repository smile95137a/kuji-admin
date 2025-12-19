package com.group.admin.mapper;

import com.group.admin.entity.PointLog;
import com.group.admin.example.PointLogExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PointLogMapper {
    long countByExample(PointLogExample example);

    int deleteByExample(PointLogExample example);

    int deleteByPrimaryKey(String id);

    int insert(PointLog row);

    int insertSelective(PointLog row);

    List<PointLog> selectByExample(PointLogExample example);

    PointLog selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") PointLog row, @Param("example") PointLogExample example);

    int updateByExample(@Param("row") PointLog row, @Param("example") PointLogExample example);

    int updateByPrimaryKeySelective(PointLog row);

    int updateByPrimaryKey(PointLog row);
}