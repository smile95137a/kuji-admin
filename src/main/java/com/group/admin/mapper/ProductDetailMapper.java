package com.group.admin.mapper;

import com.group.admin.entity.ProductDetail;
import com.group.admin.example.ProductDetailExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ProductDetailMapper {

    int deleteByPrimaryKey(@Param("id") String id);

    int insert(ProductDetail row);

    ProductDetail selectByPrimaryKey(@Param("id") String id);

    List<ProductDetail> selectAll();

    int updateByPrimaryKey(ProductDetail row);

    List<ProductDetail> selectByExample(ProductDetailExample example);

    long countByExample(ProductDetailExample example);

    int deleteByExample(ProductDetailExample example);

}
