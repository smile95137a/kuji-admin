package com.group.admin.mapper;

import com.group.admin.entity.OrderDetail;
import com.group.admin.example.OrderDetailExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface OrderDetailMapper {

    int deleteByPrimaryKey(@Param("id") Long id);

    int insert(OrderDetail row);

    OrderDetail selectByPrimaryKey(@Param("id") Long id);

    List<OrderDetail> selectAll();

    int updateByPrimaryKey(OrderDetail row);

    List<OrderDetail> selectByExample(OrderDetailExample example);

    long countByExample(OrderDetailExample example);

    int deleteByExample(OrderDetailExample example);

}
