package com.group.admin.mapper;

import com.group.admin.entity.RechargePlan;
import com.group.admin.example.RechargePlanExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface RechargePlanMapper {
    long countByExample(RechargePlanExample example);

    int deleteByExample(RechargePlanExample example);

    int deleteByPrimaryKey(String id);

    int insert(RechargePlan row);

    int insertSelective(RechargePlan row);

    List<RechargePlan> selectByExample(RechargePlanExample example);

    RechargePlan selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") RechargePlan row, @Param("example") RechargePlanExample example);

    int updateByExample(@Param("row") RechargePlan row, @Param("example") RechargePlanExample example);

    int updateByPrimaryKeySelective(RechargePlan row);

    int updateByPrimaryKey(RechargePlan row);
}