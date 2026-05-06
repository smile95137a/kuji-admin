package com.group.admin.mapper;

import com.group.admin.entity.LotteryThemeAlias;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LotteryThemeAliasMapper {
    LotteryThemeAlias selectById(@Param("id") String id);

    LotteryThemeAlias selectByThemeIdAndAliasName(@Param("themeId") String themeId, @Param("aliasName") String aliasName);

    LotteryThemeAlias selectByNormalizedName(@Param("normalizedName") String normalizedName);

    List<LotteryThemeAlias> selectByThemeId(@Param("themeId") String themeId, @Param("status") String status);

    int insert(LotteryThemeAlias row);

    int updateByPrimaryKeySelective(LotteryThemeAlias row);

    int softDeleteById(@Param("id") String id);
}
