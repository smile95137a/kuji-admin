package com.group.admin.mapper;

import com.group.admin.entity.ProductDetailShippingMethod;
import com.group.admin.example.ProductDetailShippingMethodExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ProductDetailShippingMethodMapper {

    int deleteByPrimaryKey(@Param("id") String id);

    int insert(ProductDetailShippingMethod row);

    ProductDetailShippingMethod selectByPrimaryKey(@Param("id") String id);

    List<ProductDetailShippingMethod> selectAll();

    int updateByPrimaryKey(ProductDetailShippingMethod row);

    List<ProductDetailShippingMethod> selectByExample(ProductDetailShippingMethodExample example);

    long countByExample(ProductDetailShippingMethodExample example);

    int deleteByExample(ProductDetailShippingMethodExample example);

}
