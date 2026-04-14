package com.group.admin.mapper;

import com.group.admin.entity.ShippingMethod;
import com.group.admin.example.ShippingMethodExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ShippingMethodMapper {
    long countByExample(ShippingMethodExample example);

    int deleteByExample(ShippingMethodExample example);

    int deleteByPrimaryKey(String id);

    int insert(ShippingMethod row);

    int insertSelective(ShippingMethod row);

    List<ShippingMethod> selectByExample(ShippingMethodExample example);

    ShippingMethod selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") ShippingMethod row, @Param("example") ShippingMethodExample example);

    int updateByExample(@Param("row") ShippingMethod row, @Param("example") ShippingMethodExample example);

    int updateByPrimaryKeySelective(ShippingMethod row);

    int updateByPrimaryKey(ShippingMethod row);
}
