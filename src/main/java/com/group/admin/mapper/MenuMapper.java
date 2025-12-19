package com.group.admin.mapper;

import com.group.admin.entity.Menu;
import com.group.admin.example.MenuExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface MenuMapper {

    int deleteByPrimaryKey(@Param("id") String id);

    int insert(Menu row);

    Menu selectByPrimaryKey(@Param("id") String id);

    List<Menu> selectAll();

    int updateByPrimaryKey(Menu row);

    List<Menu> selectByExample(MenuExample example);

    long countByExample(MenuExample example);

    int deleteByExample(MenuExample example);

}
