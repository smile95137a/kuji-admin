package com.group.admin.mapper;

import com.group.admin.entity.OrderItem;
import com.group.admin.example.OrderItemExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface OrderItemMapper {
    long countByExample(OrderItemExample example);

    int deleteByExample(OrderItemExample example);

    int deleteByPrimaryKey(String id);

    int insert(OrderItem row);

    int insertSelective(OrderItem row);

    List<OrderItem> selectByExample(OrderItemExample example);

    OrderItem selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") OrderItem row, @Param("example") OrderItemExample example);

    int updateByExample(@Param("row") OrderItem row, @Param("example") OrderItemExample example);

    int updateByPrimaryKeySelective(OrderItem row);

    int updateByPrimaryKey(OrderItem row);
}