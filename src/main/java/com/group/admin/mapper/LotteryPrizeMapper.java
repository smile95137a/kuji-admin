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

    /** Pessimistic lock: SELECT ... FOR UPDATE */
    LotteryPrize selectByPrimaryKeyForUpdate(String id);

    /** Count grand prizes with remaining stock > 0 */
    int countGrandPrizesWithStock(String lotteryId);

    /** Get last prize (is_last_prize=1 and remaining>0) */
    LotteryPrize selectLastPrize(String lotteryId);

    /** Get all prizes with remaining stock, ordered by order_num */
    List<LotteryPrize> selectAvailablePrizes(String lotteryId);
}