package com.group.admin.mapper;

import com.group.admin.entity.OrderLog;
import com.group.admin.example.OrderLogExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface OrderLogMapper {

    int deleteByPrimaryKey(@Param("id") Long id);

    int insert(OrderLog row);

    OrderLog selectByPrimaryKey(@Param("id") Long id);

    List<OrderLog> selectAll();

    int updateByPrimaryKey(OrderLog row);

    List<OrderLog> selectByExample(OrderLogExample example);

    long countByExample(OrderLogExample example);

    int deleteByExample(OrderLogExample example);

}
