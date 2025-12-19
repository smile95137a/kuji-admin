package com.group.admin.mapper;

import com.group.admin.entity.AdminUser;
import com.group.admin.example.AdminUserExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AdminUserMapper {

    int deleteByPrimaryKey(@Param("id") String id);

    int insert(AdminUser row);

    AdminUser selectByPrimaryKey(@Param("id") String id);

    List<AdminUser> selectAll();

    int updateByPrimaryKey(AdminUser row);

    List<AdminUser> selectByExample(AdminUserExample example);

    long countByExample(AdminUserExample example);

    int deleteByExample(AdminUserExample example);

}
