package com.group.admin.mapper;

import com.group.admin.entity.RoleMenu;
import com.group.admin.example.RoleMenuExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface RoleMenuMapper {
    long countByExample(RoleMenuExample example);

    int deleteByExample(RoleMenuExample example);

    int deleteByPrimaryKey(String id);

    int insert(RoleMenu row);

    int insertSelective(RoleMenu row);

    List<RoleMenu> selectByExample(RoleMenuExample example);

    RoleMenu selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") RoleMenu row, @Param("example") RoleMenuExample example);

    int updateByExample(@Param("row") RoleMenu row, @Param("example") RoleMenuExample example);

    int updateByPrimaryKeySelective(RoleMenu row);

    int updateByPrimaryKey(RoleMenu row);

    List<RoleMenu> selectByRoleId(String roleId);

    int deleteByRoleId(String roleId);

    int batchInsert(@Param("list") List<RoleMenu> list);
}