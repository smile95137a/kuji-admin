package com.group.admin.mapper;

import com.group.admin.entity.AdminUserRole;
import com.group.admin.example.AdminUserRoleExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AdminUserRoleMapper {
    long countByExample(AdminUserRoleExample example);

    int deleteByExample(AdminUserRoleExample example);

    int deleteByPrimaryKey(String id);

    int insert(AdminUserRole row);

    int insertSelective(AdminUserRole row);

    List<AdminUserRole> selectByExample(AdminUserRoleExample example);

    AdminUserRole selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") AdminUserRole row, @Param("example") AdminUserRoleExample example);

    int updateByExample(@Param("row") AdminUserRole row, @Param("example") AdminUserRoleExample example);

    int updateByPrimaryKeySelective(AdminUserRole row);

    int updateByPrimaryKey(AdminUserRole row);
}