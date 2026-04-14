package com.group.admin.mapper;

import com.group.admin.entity.LotteryLock;
import com.group.admin.example.LotteryLockExample;
import java.time.LocalDateTime;
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

    /** Get active lock for a lottery (is_active=1 and lock_end_time > NOW()) */
    LotteryLock selectActiveLock(String lotteryId);

    /** Batch expire stale locks where lock_end_time < now */
    int expireStaleLocksBeforeTime(@Param("now") LocalDateTime now);
}