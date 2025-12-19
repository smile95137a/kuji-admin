package com.group.admin.mapper;

import com.group.admin.entity.PrizeNumber;
import com.group.admin.example.PrizeNumberExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface PrizeNumberMapper {

    int deleteByPrimaryKey(@Param("id") String id);

    int insert(PrizeNumber row);

    PrizeNumber selectByPrimaryKey(@Param("id") String id);

    List<PrizeNumber> selectAll();

    int updateByPrimaryKey(PrizeNumber row);

    List<PrizeNumber> selectByExample(PrizeNumberExample example);

    long countByExample(PrizeNumberExample example);

    int deleteByExample(PrizeNumberExample example);

}
