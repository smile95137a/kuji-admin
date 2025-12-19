package com.group.admin.mapper;

import com.group.admin.entity.News;
import com.group.admin.example.NewsExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface NewsMapper {

    int deleteByPrimaryKey(@Param("id") Long id);

    int insert(News row);

    News selectByPrimaryKey(@Param("id") Long id);

    List<News> selectAll();

    int updateByPrimaryKey(News row);

    List<News> selectByExample(NewsExample example);

    long countByExample(NewsExample example);

    int deleteByExample(NewsExample example);

}
