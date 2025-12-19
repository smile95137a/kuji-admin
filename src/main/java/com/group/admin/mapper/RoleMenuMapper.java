package com.group.admin.mapper;

import com.group.admin.entity.RoleMenu;
import com.group.admin.example.RoleMenuExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface RoleMenuMapper {

    int deleteByPrimaryKey(@Param("id") String id);

    int insert(RoleMenu row);

    RoleMenu selectByPrimaryKey(@Param("id") String id);

    List<RoleMenu> selectAll();

    int updateByPrimaryKey(RoleMenu row);

    List<RoleMenu> selectByExample(RoleMenuExample example);

    long countByExample(RoleMenuExample example);

    int deleteByExample(RoleMenuExample example);

}
