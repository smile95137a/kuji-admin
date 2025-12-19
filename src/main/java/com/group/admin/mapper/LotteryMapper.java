package com.group.admin.mapper;

import com.group.admin.entity.Lottery;
import com.group.admin.example.LotteryExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface LotteryMapper {

    int deleteByPrimaryKey(@Param("id") String id);

    int insert(Lottery row);

    Lottery selectByPrimaryKey(@Param("id") String id);

    List<Lottery> selectAll();

    int updateByPrimaryKey(Lottery row);

    List<Lottery> selectByExample(LotteryExample example);

    long countByExample(LotteryExample example);

    int deleteByExample(LotteryExample example);

}
