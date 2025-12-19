package com.group.admin.mapper;

import com.group.admin.entity.StoreProductRecommendation;
import com.group.admin.example.StoreProductRecommendationExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface StoreProductRecommendationMapper {

    int deleteByPrimaryKey(@Param("id") Long id);

    int insert(StoreProductRecommendation row);

    StoreProductRecommendation selectByPrimaryKey(@Param("id") Long id);

    List<StoreProductRecommendation> selectAll();

    int updateByPrimaryKey(StoreProductRecommendation row);

    List<StoreProductRecommendation> selectByExample(StoreProductRecommendationExample example);

    long countByExample(StoreProductRecommendationExample example);

    int deleteByExample(StoreProductRecommendationExample example);

}
