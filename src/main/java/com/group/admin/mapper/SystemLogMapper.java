package com.group.admin.mapper;

import com.group.admin.entity.SystemLog;
import com.group.admin.example.SystemLogExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SystemLogMapper {
    long countByExample(SystemLogExample example);

    int deleteByExample(SystemLogExample example);

    int deleteByPrimaryKey(String id);

    int insert(SystemLog row);

    int insertSelective(SystemLog row);

    List<SystemLog> selectByExampleWithBLOBs(SystemLogExample example);

    List<SystemLog> selectByExample(SystemLogExample example);

    SystemLog selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") SystemLog row, @Param("example") SystemLogExample example);

    int updateByExampleWithBLOBs(@Param("row") SystemLog row, @Param("example") SystemLogExample example);

    int updateByExample(@Param("row") SystemLog row, @Param("example") SystemLogExample example);

    int updateByPrimaryKeySelective(SystemLog row);

    int updateByPrimaryKeyWithBLOBs(SystemLog row);

    int updateByPrimaryKey(SystemLog row);
}