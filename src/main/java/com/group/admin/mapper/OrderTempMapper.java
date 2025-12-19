package com.group.admin.mapper;

import com.group.admin.entity.OrderTemp;
import com.group.admin.example.OrderTempExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface OrderTempMapper {

    int deleteByPrimaryKey(@Param("id") Integer id);

    int insert(OrderTemp row);

    OrderTemp selectByPrimaryKey(@Param("id") Integer id);

    List<OrderTemp> selectAll();

    int updateByPrimaryKey(OrderTemp row);

    List<OrderTemp> selectByExample(OrderTempExample example);

    long countByExample(OrderTempExample example);

    int deleteByExample(OrderTempExample example);

}
