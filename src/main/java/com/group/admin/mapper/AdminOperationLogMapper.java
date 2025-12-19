package com.group.admin.mapper;

import com.group.admin.entity.AdminOperationLog;
import com.group.admin.example.AdminOperationLogExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AdminOperationLogMapper {

    int deleteByPrimaryKey(@Param("id") String id);

    int insert(AdminOperationLog row);

    AdminOperationLog selectByPrimaryKey(@Param("id") String id);

    List<AdminOperationLog> selectAll();

    int updateByPrimaryKey(AdminOperationLog row);

    List<AdminOperationLog> selectByExample(AdminOperationLogExample example);

    long countByExample(AdminOperationLogExample example);

    int deleteByExample(AdminOperationLogExample example);

}
