package com.group.admin.mapper;

import com.group.admin.entity.LotteryPrize;
import com.group.admin.example.LotteryPrizeExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface LotteryPrizeMapper {

    int deleteByPrimaryKey(@Param("id") String id);

    int insert(LotteryPrize row);

    LotteryPrize selectByPrimaryKey(@Param("id") String id);

    List<LotteryPrize> selectAll();

    int updateByPrimaryKey(LotteryPrize row);

    List<LotteryPrize> selectByExample(LotteryPrizeExample example);

    long countByExample(LotteryPrizeExample example);

    int deleteByExample(LotteryPrizeExample example);

}
