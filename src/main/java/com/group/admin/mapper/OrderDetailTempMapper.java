package com.group.admin.mapper;

import com.group.admin.entity.OrderDetailTemp;
import com.group.admin.example.OrderDetailTempExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface OrderDetailTempMapper {

    int deleteByPrimaryKey(@Param("id") Long id);

    int insert(OrderDetailTemp row);

    OrderDetailTemp selectByPrimaryKey(@Param("id") Long id);

    List<OrderDetailTemp> selectAll();

    int updateByPrimaryKey(OrderDetailTemp row);

    List<OrderDetailTemp> selectByExample(OrderDetailTempExample example);

    long countByExample(OrderDetailTempExample example);

    int deleteByExample(OrderDetailTempExample example);

}
