package com.group.admin.mapper;

import com.group.admin.entity.ConsumptionRecord;
import com.group.admin.example.ConsumptionRecordExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ConsumptionRecordMapper {
    long countByExample(ConsumptionRecordExample example);

    int deleteByExample(ConsumptionRecordExample example);

    int deleteByPrimaryKey(String id);

    int insert(ConsumptionRecord row);

    int insertSelective(ConsumptionRecord row);

    List<ConsumptionRecord> selectByExample(ConsumptionRecordExample example);

    ConsumptionRecord selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") ConsumptionRecord row, @Param("example") ConsumptionRecordExample example);

    int updateByExample(@Param("row") ConsumptionRecord row, @Param("example") ConsumptionRecordExample example);

    int updateByPrimaryKeySelective(ConsumptionRecord row);

    int updateByPrimaryKey(ConsumptionRecord row);
}