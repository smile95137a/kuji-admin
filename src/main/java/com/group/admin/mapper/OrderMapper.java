package com.group.admin.mapper;

import com.group.admin.entity.Order;
import com.group.admin.example.OrderExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface OrderMapper {
    long countByExample(OrderExample example);

    int deleteByExample(OrderExample example);

    int deleteByPrimaryKey(String id);

    int insert(Order row);

    int insertSelective(Order row);

    List<Order> selectByExample(OrderExample example);

    Order selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") Order row, @Param("example") OrderExample example);

    int updateByExample(@Param("row") Order row, @Param("example") OrderExample example);

    int updateByPrimaryKeySelective(Order row);

    int updateByPrimaryKey(Order row);
}