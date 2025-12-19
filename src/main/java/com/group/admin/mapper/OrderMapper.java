package com.group.admin.mapper;

import com.group.admin.entity.Order;
import com.group.admin.example.OrderExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface OrderMapper {

    int deleteByPrimaryKey(@Param("id") Integer id);

    int insert(Order row);

    Order selectByPrimaryKey(@Param("id") Integer id);

    List<Order> selectAll();

    int updateByPrimaryKey(Order row);

    List<Order> selectByExample(OrderExample example);

    long countByExample(OrderExample example);

    int deleteByExample(OrderExample example);

}
