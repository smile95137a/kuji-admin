package com.group.admin.mapper;

import com.group.admin.entity.LotteryTag;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LotteryTagMapper {
    LotteryTag selectById(@Param("id") String id);

    LotteryTag selectByName(@Param("name") String name);

    LotteryTag selectByNameIgnoreCase(@Param("name") String name);

    LotteryTag selectByNormalizedName(@Param("normalizedName") String normalizedName);

    List<LotteryTag> selectAll(@Param("status") String status);

    int insert(LotteryTag row);

    int updateByPrimaryKeySelective(LotteryTag row);

    int deleteById(@Param("id") String id);
}
