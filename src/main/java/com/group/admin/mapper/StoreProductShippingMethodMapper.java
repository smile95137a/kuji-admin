package com.group.admin.mapper;

import com.group.admin.entity.StoreProductShippingMethod;
import com.group.admin.example.StoreProductShippingMethodExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface StoreProductShippingMethodMapper {

    int deleteByPrimaryKey(@Param("id") String id);

    int insert(StoreProductShippingMethod row);

    StoreProductShippingMethod selectByPrimaryKey(@Param("id") String id);

    List<StoreProductShippingMethod> selectAll();

    int updateByPrimaryKey(StoreProductShippingMethod row);

    List<StoreProductShippingMethod> selectByExample(StoreProductShippingMethodExample example);

    long countByExample(StoreProductShippingMethodExample example);

    int deleteByExample(StoreProductShippingMethodExample example);

}
