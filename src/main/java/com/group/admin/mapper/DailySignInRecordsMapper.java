package com.group.admin.mapper;

import com.group.admin.entity.DailySignInRecords;
import com.group.admin.example.DailySignInRecordsExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface DailySignInRecordsMapper {

    int deleteByPrimaryKey(@Param("id") Long id);

    int insert(DailySignInRecords row);

    DailySignInRecords selectByPrimaryKey(@Param("id") Long id);

    List<DailySignInRecords> selectAll();

    int updateByPrimaryKey(DailySignInRecords row);

    List<DailySignInRecords> selectByExample(DailySignInRecordsExample example);

    long countByExample(DailySignInRecordsExample example);

    int deleteByExample(DailySignInRecordsExample example);

}
