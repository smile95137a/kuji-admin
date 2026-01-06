package com.group.admin.mapper;

import com.group.admin.entity.News;
import com.group.admin.example.NewsExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface NewsMapper {
    long countByExample(NewsExample example);

    int deleteByExample(NewsExample example);

    int deleteByPrimaryKey(String id);

    int insert(News row);

    int insertSelective(News row);

    List<News> selectByExampleWithBLOBs(NewsExample example);

    List<News> selectByExample(NewsExample example);

    News selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") News row, @Param("example") NewsExample example);

    int updateByExampleWithBLOBs(@Param("row") News row, @Param("example") NewsExample example);

    int updateByExample(@Param("row") News row, @Param("example") NewsExample example);

    int updateByPrimaryKeySelective(News row);

    int updateByPrimaryKeyWithBLOBs(News row);

    int updateByPrimaryKey(News row);
}