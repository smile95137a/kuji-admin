package com.group.admin.mapper;

import com.group.admin.entity.User;
import com.group.admin.example.UserExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    long countByExample(UserExample example);

    int deleteByExample(UserExample example);

    int deleteByPrimaryKey(String id);

    int insert(User row);

    int insertSelective(User row);

    List<User> selectByExample(UserExample example);

    User selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") User row, @Param("example") UserExample example);

    int updateByExample(@Param("row") User row, @Param("example") UserExample example);

    int updateByPrimaryKeySelective(User row);

    int updateByPrimaryKey(User row);

    int updateBalanceWithVersion(@Param("userId") String userId,
                                  @Param("goldCoins") Long goldCoins,
                                  @Param("bonusCoins") Long bonusCoins,
                                  @Param("totalRecharged") Long totalRecharged,
                                  @Param("version") Integer version);
}