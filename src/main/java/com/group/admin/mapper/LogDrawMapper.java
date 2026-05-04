package com.group.admin.mapper;

import com.group.admin.entity.LogDraw;
import com.group.admin.example.LogDrawExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface LogDrawMapper {
    long countByExample(LogDrawExample example);

    int deleteByExample(LogDrawExample example);

    int deleteByPrimaryKey(String id);

    int insert(LogDraw row);

    int insertSelective(LogDraw row);

    List<LogDraw> selectByExample(LogDrawExample example);

    LogDraw selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") LogDraw row, @Param("example") LogDrawExample example);

    int updateByExample(@Param("row") LogDraw row, @Param("example") LogDrawExample example);

    int updateByPrimaryKeySelective(LogDraw row);

    int updateByPrimaryKey(LogDraw row);
}
