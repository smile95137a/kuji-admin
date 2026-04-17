package com.group.admin.mapper;

import com.group.admin.entity.UserTokenBlacklist;
import com.group.admin.example.UserTokenBlacklistExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UserTokenBlacklistMapper {
    long countByExample(UserTokenBlacklistExample example);

    int deleteByExample(UserTokenBlacklistExample example);

    int deleteByPrimaryKey(String userId);

    int insert(UserTokenBlacklist row);

    int insertSelective(UserTokenBlacklist row);

    List<UserTokenBlacklist> selectByExample(UserTokenBlacklistExample example);

    UserTokenBlacklist selectByPrimaryKey(String userId);

    int updateByExampleSelective(@Param("row") UserTokenBlacklist row, @Param("example") UserTokenBlacklistExample example);

    int updateByExample(@Param("row") UserTokenBlacklist row, @Param("example") UserTokenBlacklistExample example);

    int updateByPrimaryKeySelective(UserTokenBlacklist row);

    int updateByPrimaryKey(UserTokenBlacklist row);

    int upsert(UserTokenBlacklist row);

    int incrementBlacklistGen(@Param("userId") String userId);
}
