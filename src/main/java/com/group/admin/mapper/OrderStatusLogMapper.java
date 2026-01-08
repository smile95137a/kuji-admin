package com.group.admin.mapper;

import com.group.admin.entity.OrderStatusLog;
import com.group.admin.example.OrderStatusLogExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface OrderStatusLogMapper {
    long countByExample(OrderStatusLogExample example);

    int deleteByExample(OrderStatusLogExample example);

    int deleteByPrimaryKey(String id);

    int insert(OrderStatusLog row);

    int insertSelective(OrderStatusLog row);

    List<OrderStatusLog> selectByExample(OrderStatusLogExample example);

    OrderStatusLog selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") OrderStatusLog row, @Param("example") OrderStatusLogExample example);

    int updateByExample(@Param("row") OrderStatusLog row, @Param("example") OrderStatusLogExample example);

    int updateByPrimaryKeySelective(OrderStatusLog row);

    int updateByPrimaryKey(OrderStatusLog row);
}