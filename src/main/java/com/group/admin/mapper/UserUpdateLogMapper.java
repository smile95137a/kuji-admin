package com.group.admin.mapper;

import com.group.admin.entity.UserUpdateLog;
import com.group.admin.example.UserUpdateLogExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserUpdateLogMapper {

    int deleteByPrimaryKey(@Param("id") Long id);

    int insert(UserUpdateLog row);

    UserUpdateLog selectByPrimaryKey(@Param("id") Long id);

    List<UserUpdateLog> selectAll();

    int updateByPrimaryKey(UserUpdateLog row);

    List<UserUpdateLog> selectByExample(UserUpdateLogExample example);

    long countByExample(UserUpdateLogExample example);

    int deleteByExample(UserUpdateLogExample example);

}
