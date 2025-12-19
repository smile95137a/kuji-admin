package com.group.admin.mapper;

import com.group.admin.entity.LotteryDrawRecord;
import com.group.admin.example.LotteryDrawRecordExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface LotteryDrawRecordMapper {

    int deleteByPrimaryKey(@Param("id") String id);

    int insert(LotteryDrawRecord row);

    LotteryDrawRecord selectByPrimaryKey(@Param("id") String id);

    List<LotteryDrawRecord> selectAll();

    int updateByPrimaryKey(LotteryDrawRecord row);

    List<LotteryDrawRecord> selectByExample(LotteryDrawRecordExample example);

    long countByExample(LotteryDrawRecordExample example);

    int deleteByExample(LotteryDrawRecordExample example);

}
