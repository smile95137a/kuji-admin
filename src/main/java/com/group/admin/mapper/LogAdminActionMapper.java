package com.group.admin.mapper;

import com.group.admin.entity.LogAdminAction;
import com.group.admin.example.LogAdminActionExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface LogAdminActionMapper {
    long countByExample(LogAdminActionExample example);

    int deleteByExample(LogAdminActionExample example);

    int deleteByPrimaryKey(String id);

    int insert(LogAdminAction row);

    int insertSelective(LogAdminAction row);

    List<LogAdminAction> selectByExample(LogAdminActionExample example);

    LogAdminAction selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") LogAdminAction row, @Param("example") LogAdminActionExample example);

    int updateByExample(@Param("row") LogAdminAction row, @Param("example") LogAdminActionExample example);

    int updateByPrimaryKeySelective(LogAdminAction row);

    int updateByPrimaryKey(LogAdminAction row);
}
