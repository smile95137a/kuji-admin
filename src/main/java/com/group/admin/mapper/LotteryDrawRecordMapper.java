package com.group.admin.mapper;

import com.group.admin.entity.LotteryDrawRecord;
import com.group.admin.example.LotteryDrawRecordExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface LotteryDrawRecordMapper {
    long countByExample(LotteryDrawRecordExample example);

    int deleteByExample(LotteryDrawRecordExample example);

    int deleteByPrimaryKey(String id);

    int insert(LotteryDrawRecord row);

    int insertSelective(LotteryDrawRecord row);

    List<LotteryDrawRecord> selectByExample(LotteryDrawRecordExample example);

    LotteryDrawRecord selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") LotteryDrawRecord row, @Param("example") LotteryDrawRecordExample example);

    int updateByExample(@Param("row") LotteryDrawRecord row, @Param("example") LotteryDrawRecordExample example);

    int updateByPrimaryKeySelective(LotteryDrawRecord row);

    int updateByPrimaryKey(LotteryDrawRecord row);

    List<java.util.Map<String, Object>> selectByLotteryIdPaged(
        @Param("lotteryId") String lotteryId,
        @Param("userId") String userId,
        @Param("status") String status,
        @Param("startDate") java.time.LocalDateTime startDate,
        @Param("endDate") java.time.LocalDateTime endDate,
        @Param("limit") int limit,
        @Param("offset") int offset);

    long countByLotteryIdFiltered(
        @Param("lotteryId") String lotteryId,
        @Param("userId") String userId,
        @Param("status") String status,
        @Param("startDate") java.time.LocalDateTime startDate,
        @Param("endDate") java.time.LocalDateTime endDate);

    Long sumCostAmountByLotteryId(String lotteryId);
}