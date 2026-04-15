package com.group.admin.mapper;

import com.group.admin.entity.LotteryLock;
import com.group.admin.example.LotteryLockExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface LotteryLockMapper {
    long countByExample(LotteryLockExample example);

    int deleteByExample(LotteryLockExample example);

    int deleteByPrimaryKey(String id);

    int insert(LotteryLock row);

    int insertSelective(LotteryLock row);

    List<LotteryLock> selectByExample(LotteryLockExample example);

    LotteryLock selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") LotteryLock row, @Param("example") LotteryLockExample example);

    int updateByExample(@Param("row") LotteryLock row, @Param("example") LotteryLockExample example);

    int updateByPrimaryKeySelective(LotteryLock row);

    int updateByPrimaryKey(LotteryLock row);

    // ---------- custom methods ----------

    LotteryLock selectActiveLock(@Param("lotteryId") String lotteryId);

    int expireStaleLocksBeforeTime(@Param("expireTime") java.time.LocalDateTime expireTime);
}