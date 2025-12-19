package com.group.admin.mapper;

import com.group.admin.entity.Role;
import com.group.admin.example.RoleExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface RoleMapper {

    int deleteByPrimaryKey(@Param("id") String id);

    int insert(Role row);

    Role selectByPrimaryKey(@Param("id") String id);

    List<Role> selectAll();

    int updateByPrimaryKey(Role row);

    List<Role> selectByExample(RoleExample example);

    long countByExample(RoleExample example);

    int deleteByExample(RoleExample example);

    /**
     * 根據角色代碼查詢角色
     */
    Role selectByCode(@Param("code") String code);

}
