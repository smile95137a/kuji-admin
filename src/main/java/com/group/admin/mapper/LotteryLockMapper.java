package com.group.admin.mapper;

import com.group.admin.entity.LotteryLock;
import com.group.admin.example.LotteryLockExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface LotteryLockMapper {

    int deleteByPrimaryKey(@Param("id") String id);

    int insert(LotteryLock row);

    LotteryLock selectByPrimaryKey(@Param("id") String id);

    List<LotteryLock> selectAll();

    int updateByPrimaryKey(LotteryLock row);

    List<LotteryLock> selectByExample(LotteryLockExample example);

    long countByExample(LotteryLockExample example);

    int deleteByExample(LotteryLockExample example);

}
