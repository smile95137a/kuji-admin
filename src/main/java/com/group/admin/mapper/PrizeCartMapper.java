package com.group.admin.mapper;

import com.group.admin.entity.PrizeCart;
import com.group.admin.example.PrizeCartExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface PrizeCartMapper {

    int deleteByPrimaryKey(@Param("id") String id);

    int insert(PrizeCart row);

    PrizeCart selectByPrimaryKey(@Param("id") String id);

    List<PrizeCart> selectAll();

    int updateByPrimaryKey(PrizeCart row);

    List<PrizeCart> selectByExample(PrizeCartExample example);

    long countByExample(PrizeCartExample example);

    int deleteByExample(PrizeCartExample example);

}
