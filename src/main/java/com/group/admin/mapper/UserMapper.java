package com.group.admin.mapper;

import com.group.admin.entity.User;
import com.group.admin.example.UserExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 前台玩家 Mapper
 * 所有 ID 為 VARCHAR(36) UUID
 */
@Mapper
public interface UserMapper {

    int deleteByPrimaryKey(@Param("id") String id);

    int insert(User row);

    User selectByPrimaryKey(@Param("id") String id);

    List<User> selectAll();

    int updateByPrimaryKey(User row);

    int updateByPrimaryKeySelective(User row);

    List<User> selectByExample(UserExample example);

    long countByExample(UserExample example);

    int deleteByExample(UserExample example);

}
