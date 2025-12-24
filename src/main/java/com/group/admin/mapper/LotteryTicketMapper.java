package com.group.admin.mapper;

import com.group.admin.entity.LotteryTicket;
import com.group.admin.example.LotteryTicketExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface LotteryTicketMapper {
    long countByExample(LotteryTicketExample example);

    int deleteByExample(LotteryTicketExample example);

    int deleteByPrimaryKey(String id);

    int insert(LotteryTicket row);

    int insertSelective(LotteryTicket row);

    List<LotteryTicket> selectByExample(LotteryTicketExample example);

    LotteryTicket selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") LotteryTicket row, @Param("example") LotteryTicketExample example);

    int updateByExample(@Param("row") LotteryTicket row, @Param("example") LotteryTicketExample example);

    int updateByPrimaryKeySelective(LotteryTicket row);

    int updateByPrimaryKey(LotteryTicket row);
}