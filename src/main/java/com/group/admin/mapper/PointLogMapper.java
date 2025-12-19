package com.group.admin.mapper;

import com.group.admin.entity.PointLog;
import com.group.admin.example.PointLogExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface PointLogMapper {

    int deleteByPrimaryKey(@Param("id") String id);

    int insert(PointLog row);

    PointLog selectByPrimaryKey(@Param("id") String id);

    List<PointLog> selectAll();

    int updateByPrimaryKey(PointLog row);

    List<PointLog> selectByExample(PointLogExample example);

    long countByExample(PointLogExample example);

    int deleteByExample(PointLogExample example);

}
