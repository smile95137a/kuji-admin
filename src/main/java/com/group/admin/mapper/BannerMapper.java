package com.group.admin.mapper;

import com.group.admin.entity.Banner;
import com.group.admin.example.BannerExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface BannerMapper {
    long countByExample(BannerExample example);

    int deleteByExample(BannerExample example);

    int deleteByPrimaryKey(String id);

    int insert(Banner row);

    int insertSelective(Banner row);

    List<Banner> selectByExample(BannerExample example);

    Banner selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") Banner row, @Param("example") BannerExample example);

    int updateByExample(@Param("row") Banner row, @Param("example") BannerExample example);

    int updateByPrimaryKeySelective(Banner row);

    int updateByPrimaryKey(Banner row);
}