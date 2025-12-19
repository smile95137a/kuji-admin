package com.group.admin.mapper;

import com.group.admin.entity.Role;
import com.group.admin.example.RoleExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface RoleMapper {
    long countByExample(RoleExample example);

    int deleteByExample(RoleExample example);

    int deleteByPrimaryKey(String id);

    int insert(Role row);

    int insertSelective(Role row);

    List<Role> selectByExample(RoleExample example);

    Role selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") Role row, @Param("example") RoleExample example);

    int updateByExample(@Param("row") Role row, @Param("example") RoleExample example);

    int updateByPrimaryKeySelective(Role row);

    int updateByPrimaryKey(Role row);
}