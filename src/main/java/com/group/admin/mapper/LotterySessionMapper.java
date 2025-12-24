package com.group.admin.mapper;

import com.group.admin.entity.LotterySession;
import com.group.admin.example.LotterySessionExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface LotterySessionMapper {
    long countByExample(LotterySessionExample example);

    int deleteByExample(LotterySessionExample example);

    int deleteByPrimaryKey(String id);

    int insert(LotterySession row);

    int insertSelective(LotterySession row);

    List<LotterySession> selectByExample(LotterySessionExample example);

    LotterySession selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") LotterySession row, @Param("example") LotterySessionExample example);

    int updateByExample(@Param("row") LotterySession row, @Param("example") LotterySessionExample example);

    int updateByPrimaryKeySelective(LotterySession row);

    int updateByPrimaryKey(LotterySession row);
}