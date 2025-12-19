package com.group.admin.mapper;

import com.group.admin.entity.PrizeCartItem;
import com.group.admin.example.PrizeCartItemExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface PrizeCartItemMapper {

    int deleteByPrimaryKey(@Param("id") String id);

    int insert(PrizeCartItem row);

    PrizeCartItem selectByPrimaryKey(@Param("id") String id);

    List<PrizeCartItem> selectAll();

    int updateByPrimaryKey(PrizeCartItem row);

    List<PrizeCartItem> selectByExample(PrizeCartItemExample example);

    long countByExample(PrizeCartItemExample example);

    int deleteByExample(PrizeCartItemExample example);

}
