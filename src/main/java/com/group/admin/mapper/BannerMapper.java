package com.group.admin.mapper;

import com.group.admin.entity.Banner;
import com.group.admin.example.BannerExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface BannerMapper {

    int deleteByPrimaryKey(@Param("id") String id);

    int insert(Banner row);

    Banner selectByPrimaryKey(@Param("id") String id);

    List<Banner> selectAll();

    int updateByPrimaryKey(Banner row);

    List<Banner> selectByExample(BannerExample example);

    long countByExample(BannerExample example);

    int deleteByExample(BannerExample example);

}
