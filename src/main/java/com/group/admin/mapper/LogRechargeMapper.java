package com.group.admin.mapper;

import com.group.admin.entity.LogRecharge;
import com.group.admin.example.LogRechargeExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface LogRechargeMapper {
    long countByExample(LogRechargeExample example);

    int deleteByExample(LogRechargeExample example);

    int deleteByPrimaryKey(String id);

    int insert(LogRecharge row);

    int insertSelective(LogRecharge row);

    List<LogRecharge> selectByExample(LogRechargeExample example);

    LogRecharge selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") LogRecharge row, @Param("example") LogRechargeExample example);

    int updateByExample(@Param("row") LogRecharge row, @Param("example") LogRechargeExample example);

    int updateByPrimaryKeySelective(LogRecharge row);

    int updateByPrimaryKey(LogRecharge row);
}
