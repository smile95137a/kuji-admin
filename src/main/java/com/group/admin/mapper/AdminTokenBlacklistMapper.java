package com.group.admin.mapper;

import com.group.admin.entity.AdminTokenBlacklist;
import org.apache.ibatis.annotations.Param;

public interface AdminTokenBlacklistMapper {
    AdminTokenBlacklist selectByAdminUserId(@Param("adminUserId") String adminUserId);
    int incrementGen(@Param("adminUserId") String adminUserId);
}
