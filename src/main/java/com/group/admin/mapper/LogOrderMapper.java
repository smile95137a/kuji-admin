package com.group.admin.mapper;

import com.group.admin.entity.LogOrder;
import com.group.admin.example.LogOrderExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface LogOrderMapper {
    long countByExample(LogOrderExample example);

    int deleteByExample(LogOrderExample example);

    int deleteByPrimaryKey(String id);

    int insert(LogOrder row);

    int insertSelective(LogOrder row);

    List<LogOrder> selectByExample(LogOrderExample example);

    LogOrder selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") LogOrder row, @Param("example") LogOrderExample example);

    int updateByExample(@Param("row") LogOrder row, @Param("example") LogOrderExample example);

    int updateByPrimaryKeySelective(LogOrder row);

    int updateByPrimaryKey(LogOrder row);
}
