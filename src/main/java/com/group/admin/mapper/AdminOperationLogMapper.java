package com.group.admin.mapper;

import com.group.admin.entity.AdminOperationLog;
import com.group.admin.example.AdminOperationLogExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AdminOperationLogMapper {
    long countByExample(AdminOperationLogExample example);

    int deleteByExample(AdminOperationLogExample example);

    int deleteByPrimaryKey(String id);

    int insert(AdminOperationLog row);

    int insertSelective(AdminOperationLog row);

    List<AdminOperationLog> selectByExampleWithBLOBs(AdminOperationLogExample example);

    List<AdminOperationLog> selectByExample(AdminOperationLogExample example);

    AdminOperationLog selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") AdminOperationLog row, @Param("example") AdminOperationLogExample example);

    int updateByExampleWithBLOBs(@Param("row") AdminOperationLog row, @Param("example") AdminOperationLogExample example);

    int updateByExample(@Param("row") AdminOperationLog row, @Param("example") AdminOperationLogExample example);

    int updateByPrimaryKeySelective(AdminOperationLog row);

    int updateByPrimaryKeyWithBLOBs(AdminOperationLog row);

    int updateByPrimaryKey(AdminOperationLog row);
}