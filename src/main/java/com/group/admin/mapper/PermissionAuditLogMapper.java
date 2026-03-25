package com.group.admin.mapper;

import com.group.admin.entity.PermissionAuditLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 權限審計日誌 Mapper
 *
 * @author KUJI System
 * @since 1.0.0
 */
public interface PermissionAuditLogMapper {

    int insert(PermissionAuditLog log);

    List<PermissionAuditLog> selectByRoleId(@Param("roleId") String roleId,
                                            @Param("offset") int offset,
                                            @Param("limit") int limit);
}
