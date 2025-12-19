package com.group.admin.mapper;

import com.group.admin.entity.UserReward;
import com.group.admin.example.UserRewardExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserRewardMapper {

    int deleteByPrimaryKey(@Param("id") Long id);

    int insert(UserReward row);

    UserReward selectByPrimaryKey(@Param("id") Long id);

    List<UserReward> selectAll();

    int updateByPrimaryKey(UserReward row);

    List<UserReward> selectByExample(UserRewardExample example);

    long countByExample(UserRewardExample example);

    int deleteByExample(UserRewardExample example);

}
