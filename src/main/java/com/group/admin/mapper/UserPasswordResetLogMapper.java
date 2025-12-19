package com.group.admin.mapper;

import com.group.admin.entity.UserPasswordResetLog;
import com.group.admin.example.UserPasswordResetLogExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserPasswordResetLogMapper {

    int deleteByPrimaryKey(@Param("id") Long id);

    int insert(UserPasswordResetLog row);

    UserPasswordResetLog selectByPrimaryKey(@Param("id") Long id);

    List<UserPasswordResetLog> selectAll();

    int updateByPrimaryKey(UserPasswordResetLog row);

    List<UserPasswordResetLog> selectByExample(UserPasswordResetLogExample example);

    long countByExample(UserPasswordResetLogExample example);

    int deleteByExample(UserPasswordResetLogExample example);

}
