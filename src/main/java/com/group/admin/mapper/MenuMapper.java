package com.group.admin.mapper;

import com.group.admin.entity.Menu;
import com.group.admin.example.MenuExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MenuMapper {
    long countByExample(MenuExample example);

    int deleteByExample(MenuExample example);

    int deleteByPrimaryKey(String id);

    int insert(Menu row);

    int insertSelective(Menu row);

    List<Menu> selectByExample(MenuExample example);

    Menu selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") Menu row, @Param("example") MenuExample example);

    int updateByExample(@Param("row") Menu row, @Param("example") MenuExample example);

    int updateByPrimaryKeySelective(Menu row);

    int updateByPrimaryKey(Menu row);

    // ---------- custom methods ----------

    List<java.util.Map<String, Object>> getMenusWithPermissionsForUser(@Param("userId") String userId);
}