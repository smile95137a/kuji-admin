package com.group.admin.mapper;

import com.group.admin.entity.UserLoginHistory;
import com.group.admin.example.UserLoginHistoryExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UserLoginHistoryMapper {
    long countByExample(UserLoginHistoryExample example);

    int deleteByExample(UserLoginHistoryExample example);

    int deleteByPrimaryKey(String id);

    int insert(UserLoginHistory row);

    int insertSelective(UserLoginHistory row);

    List<UserLoginHistory> selectByExample(UserLoginHistoryExample example);

    UserLoginHistory selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") UserLoginHistory row, @Param("example") UserLoginHistoryExample example);

    int updateByExample(@Param("row") UserLoginHistory row, @Param("example") UserLoginHistoryExample example);

    int updateByPrimaryKeySelective(UserLoginHistory row);

    int updateByPrimaryKey(UserLoginHistory row);
}
