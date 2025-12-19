package com.group.admin.mapper;

import com.group.admin.entity.UserRoles;
import com.group.admin.example.UserRolesExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserRolesMapper {

    int deleteByPrimaryKey(@Param("id") String id);

    int insert(UserRoles row);

    UserRoles selectByPrimaryKey(@Param("id") String id);

    List<UserRoles> selectAll();

    int updateByPrimaryKey(UserRoles row);

    List<UserRoles> selectByExample(UserRolesExample example);

    long countByExample(UserRolesExample example);

    int deleteByExample(UserRolesExample example);

}
