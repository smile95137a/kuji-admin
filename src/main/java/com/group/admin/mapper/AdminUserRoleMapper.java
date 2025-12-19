package com.group.admin.mapper;

import com.group.admin.entity.AdminUserRole;
import com.group.admin.example.AdminUserRoleExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AdminUserRoleMapper {

    int deleteByPrimaryKey(@Param("id") String id);

    int insert(AdminUserRole row);

    AdminUserRole selectByPrimaryKey(@Param("id") String id);

    List<AdminUserRole> selectAll();

    int updateByPrimaryKey(AdminUserRole row);

    List<AdminUserRole> selectByExample(AdminUserRoleExample example);

    long countByExample(AdminUserRoleExample example);

    int deleteByExample(AdminUserRoleExample example);

}
