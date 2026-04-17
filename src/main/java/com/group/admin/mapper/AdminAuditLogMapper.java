package com.group.admin.mapper;

import com.group.admin.entity.AdminAuditLog;
import com.group.admin.example.AdminAuditLogExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AdminAuditLogMapper {
    long countByExample(AdminAuditLogExample example);

    int deleteByExample(AdminAuditLogExample example);

    int deleteByPrimaryKey(String id);

    int insert(AdminAuditLog row);

    int insertSelective(AdminAuditLog row);

    List<AdminAuditLog> selectByExampleWithBLOBs(AdminAuditLogExample example);

    List<AdminAuditLog> selectByExample(AdminAuditLogExample example);

    AdminAuditLog selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") AdminAuditLog row, @Param("example") AdminAuditLogExample example);

    int updateByExampleWithBLOBs(@Param("row") AdminAuditLog row, @Param("example") AdminAuditLogExample example);

    int updateByExample(@Param("row") AdminAuditLog row, @Param("example") AdminAuditLogExample example);

    int updateByPrimaryKeySelective(AdminAuditLog row);

    int updateByPrimaryKeyWithBLOBs(AdminAuditLog row);

    int updateByPrimaryKey(AdminAuditLog row);
}
