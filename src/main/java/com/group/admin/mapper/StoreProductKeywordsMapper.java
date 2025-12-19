package com.group.admin.mapper;

import com.group.admin.entity.StoreProductKeywords;
import com.group.admin.example.StoreProductKeywordsExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface StoreProductKeywordsMapper {

    int deleteByPrimaryKey(@Param("id") Long id);

    int insert(StoreProductKeywords row);

    StoreProductKeywords selectByPrimaryKey(@Param("id") Long id);

    List<StoreProductKeywords> selectAll();

    int updateByPrimaryKey(StoreProductKeywords row);

    List<StoreProductKeywords> selectByExample(StoreProductKeywordsExample example);

    long countByExample(StoreProductKeywordsExample example);

    int deleteByExample(StoreProductKeywordsExample example);

}
