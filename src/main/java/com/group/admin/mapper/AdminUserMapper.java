package com.group.admin.mapper;

import com.group.admin.entity.AdminUser;
import com.group.admin.example.AdminUserExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AdminUserMapper {
    long countByExample(AdminUserExample example);

    int deleteByExample(AdminUserExample example);

    int deleteByPrimaryKey(String id);

    int insert(AdminUser row);

    int insertSelective(AdminUser row);

    List<AdminUser> selectByExampleWithBLOBs(AdminUserExample example);

    List<AdminUser> selectByExample(AdminUserExample example);

    AdminUser selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") AdminUser row, @Param("example") AdminUserExample example);

    int updateByExampleWithBLOBs(@Param("row") AdminUser row, @Param("example") AdminUserExample example);

    int updateByExample(@Param("row") AdminUser row, @Param("example") AdminUserExample example);

    int updateByPrimaryKeySelective(AdminUser row);

    int updateByPrimaryKeyWithBLOBs(AdminUser row);

    int updateByPrimaryKey(AdminUser row);
}