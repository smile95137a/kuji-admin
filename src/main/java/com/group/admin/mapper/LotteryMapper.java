package com.group.admin.mapper;

import com.group.admin.entity.Lottery;
import com.group.admin.example.LotteryExample;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface LotteryMapper {
    long countByExample(LotteryExample example);

    int deleteByExample(LotteryExample example);

    int deleteByPrimaryKey(String id);

    int insert(Lottery row);

    int insertSelective(Lottery row);

    List<Lottery> selectByExampleWithBLOBs(LotteryExample example);

    List<Lottery> selectByExample(LotteryExample example);

    Lottery selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") Lottery row, @Param("example") LotteryExample example);

    int updateByExampleWithBLOBs(@Param("row") Lottery row, @Param("example") LotteryExample example);

    int updateByExample(@Param("row") Lottery row, @Param("example") LotteryExample example);

    int updateByPrimaryKeySelective(Lottery row);

    int updateByPrimaryKeyWithBLOBs(Lottery row);

    int updateByPrimaryKey(Lottery row);

    /** Update price_per_draw = discounted_price after grand prize sold out */
    int updatePriceAfterGrandPrizeSoldOut(String lotteryId);

    /** Find OFF_SHELF lotteries whose scheduled_at <= now for auto on-shelf */
    List<Lottery> selectScheduledOnShelf(@Param("now") LocalDateTime now);

    /** Find ON_SHELF lotteries whose end_time <= now for auto off-shelf */
    List<Lottery> selectScheduledOffShelf(@Param("now") LocalDateTime now);
}