package com.group.admin.mapper;

import com.group.admin.entity.Cart;
import com.group.admin.example.CartExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CartMapper {

    int deleteByPrimaryKey(@Param("id") String id);

    int insert(Cart row);

    Cart selectByPrimaryKey(@Param("id") String id);

    List<Cart> selectAll();

    int updateByPrimaryKey(Cart row);

    List<Cart> selectByExample(CartExample example);

    long countByExample(CartExample example);

    int deleteByExample(CartExample example);

}
