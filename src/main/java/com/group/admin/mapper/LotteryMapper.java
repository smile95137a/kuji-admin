package com.group.admin.mapper;

import com.group.admin.entity.Lottery;
import com.group.admin.example.LotteryExample;
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

    List<Lottery> selectByExampleWithPage(
            @Param("example") LotteryExample example,
            @Param("offset") int offset,
            @Param("limit") int limit);

    Lottery selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") Lottery row, @Param("example") LotteryExample example);

    int updateByExampleWithBLOBs(@Param("row") Lottery row, @Param("example") LotteryExample example);

    int updateByExample(@Param("row") Lottery row, @Param("example") LotteryExample example);

    int updateByPrimaryKeySelective(Lottery row);

    int updateByPrimaryKeyWithBLOBs(Lottery row);

    int updateByPrimaryKey(Lottery row);

    // ---------- custom methods ----------

    void updatePriceAfterGrandPrizeSoldOut(@Param("lotteryId") String lotteryId);

    List<Lottery> selectScheduledForPromotion();

    List<Lottery> selectExpiredLotteries();

    List<Lottery> selectDrawableForStart();

    List<java.util.Map<String, Object>> selectPublicList(
            @Param("category") String category,
            @Param("storeId") String storeId,
            @Param("keyword") String keyword,
            @Param("sort") String sort,
            @Param("offset") int offset,
            @Param("limit") int limit);

    Long countPublicList(
            @Param("category") String category,
            @Param("storeId") String storeId,
            @Param("keyword") String keyword);
}