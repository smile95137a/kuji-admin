package com.group.admin.mapper;

import com.group.admin.entity.ProductRecommendationMapping;
import com.group.admin.example.ProductRecommendationMappingExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ProductRecommendationMappingMapper {

    int deleteByPrimaryKey(@Param("id") Long id);

    int insert(ProductRecommendationMapping row);

    ProductRecommendationMapping selectByPrimaryKey(@Param("id") Long id);

    List<ProductRecommendationMapping> selectAll();

    int updateByPrimaryKey(ProductRecommendationMapping row);

    List<ProductRecommendationMapping> selectByExample(ProductRecommendationMappingExample example);

    long countByExample(ProductRecommendationMappingExample example);

    int deleteByExample(ProductRecommendationMappingExample example);

}
