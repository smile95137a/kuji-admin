package com.group.admin.mapper;

import com.group.admin.entity.LotteryTheme;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LotteryThemeMapper {
    LotteryTheme selectById(@Param("id") String id);

    LotteryTheme selectByName(@Param("name") String name);

    LotteryTheme selectByNameIgnoreCase(@Param("name") String name);

    LotteryTheme selectByNormalizedName(@Param("normalizedName") String normalizedName);

    List<LotteryTheme> selectAll(@Param("status") String status);

    List<LotteryTheme> suggestByKeyword(@Param("keyword") String keyword, @Param("limit") int limit);

    int insert(LotteryTheme row);

    int updateByPrimaryKeySelective(LotteryTheme row);

    int deleteById(@Param("id") String id);

    int softDeleteById(@Param("id") String id);
}
