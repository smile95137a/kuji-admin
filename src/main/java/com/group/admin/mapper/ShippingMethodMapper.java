package com.group.admin.mapper;

import com.group.admin.entity.ShippingMethod;
import com.group.admin.example.ShippingMethodExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ShippingMethodMapper {

    int deleteByPrimaryKey(@Param("id") String id);

    int insert(ShippingMethod row);

    ShippingMethod selectByPrimaryKey(@Param("id") String id);

    List<ShippingMethod> selectAll();

    int updateByPrimaryKey(ShippingMethod row);

    List<ShippingMethod> selectByExample(ShippingMethodExample example);

    long countByExample(ShippingMethodExample example);

    int deleteByExample(ShippingMethodExample example);

}
