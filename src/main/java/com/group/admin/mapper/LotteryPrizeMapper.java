package com.group.admin.mapper;

import com.group.admin.entity.LotteryPrize;
import com.group.admin.example.LotteryPrizeExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface LotteryPrizeMapper {
    long countByExample(LotteryPrizeExample example);

    int deleteByExample(LotteryPrizeExample example);

    int deleteByPrimaryKey(String id);

    int insert(LotteryPrize row);

    int insertSelective(LotteryPrize row);

    List<LotteryPrize> selectByExampleWithBLOBs(LotteryPrizeExample example);

    List<LotteryPrize> selectByExample(LotteryPrizeExample example);

    LotteryPrize selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") LotteryPrize row, @Param("example") LotteryPrizeExample example);

    int updateByExampleWithBLOBs(@Param("row") LotteryPrize row, @Param("example") LotteryPrizeExample example);

    int updateByExample(@Param("row") LotteryPrize row, @Param("example") LotteryPrizeExample example);

    int updateByPrimaryKeySelective(LotteryPrize row);

    int updateByPrimaryKeyWithBLOBs(LotteryPrize row);

    int updateByPrimaryKey(LotteryPrize row);

    // ==================== 自訂方法 ====================

    int batchInsertPrizes(@Param("list") List<LotteryPrize> prizes);

    int deleteByLotteryId(@Param("lotteryId") String lotteryId);
}