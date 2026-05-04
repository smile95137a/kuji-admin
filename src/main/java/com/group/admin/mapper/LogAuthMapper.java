package com.group.admin.mapper;

import com.group.admin.entity.LogAuth;
import com.group.admin.example.LogAuthExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface LogAuthMapper {
    long countByExample(LogAuthExample example);

    int deleteByExample(LogAuthExample example);

    int deleteByPrimaryKey(String id);

    int insert(LogAuth row);

    int insertSelective(LogAuth row);

    List<LogAuth> selectByExample(LogAuthExample example);

    LogAuth selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") LogAuth row, @Param("example") LogAuthExample example);

    int updateByExample(@Param("row") LogAuth row, @Param("example") LogAuthExample example);

    int updateByPrimaryKeySelective(LogAuth row);

    int updateByPrimaryKey(LogAuth row);
}
