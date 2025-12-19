package com.group.admin.mapper;

import com.group.admin.entity.PrizeRecycleLog;
import com.group.admin.example.PrizeRecycleLogExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface PrizeRecycleLogMapper {

    int deleteByPrimaryKey(@Param("id") Long id);

    int insert(PrizeRecycleLog row);

    PrizeRecycleLog selectByPrimaryKey(@Param("id") Long id);

    List<PrizeRecycleLog> selectAll();

    int updateByPrimaryKey(PrizeRecycleLog row);

    List<PrizeRecycleLog> selectByExample(PrizeRecycleLogExample example);

    long countByExample(PrizeRecycleLogExample example);

    int deleteByExample(PrizeRecycleLogExample example);

}
