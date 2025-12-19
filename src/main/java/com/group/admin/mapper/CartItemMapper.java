package com.group.admin.mapper;

import com.group.admin.entity.CartItem;
import com.group.admin.example.CartItemExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CartItemMapper {

    int deleteByPrimaryKey(@Param("id") String id);

    int insert(CartItem row);

    CartItem selectByPrimaryKey(@Param("id") String id);

    List<CartItem> selectAll();

    int updateByPrimaryKey(CartItem row);

    List<CartItem> selectByExample(CartItemExample example);

    long countByExample(CartItemExample example);

    int deleteByExample(CartItemExample example);

}
