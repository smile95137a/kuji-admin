package com.group.admin.mapper;

import com.group.admin.entity.ProductCategory;
import com.group.admin.example.ProductCategoryExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ProductCategoryMapper {

    int deleteByPrimaryKey(@Param("id") String id);

    int insert(ProductCategory row);

    ProductCategory selectByPrimaryKey(@Param("id") String id);

    List<ProductCategory> selectAll();

    int updateByPrimaryKey(ProductCategory row);

    List<ProductCategory> selectByExample(ProductCategoryExample example);

    long countByExample(ProductCategoryExample example);

    int deleteByExample(ProductCategoryExample example);

}
