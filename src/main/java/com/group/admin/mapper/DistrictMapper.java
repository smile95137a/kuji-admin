package com.group.admin.mapper;

import com.group.admin.entity.District;
import com.group.admin.example.DistrictExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DistrictMapper {
    long countByExample(DistrictExample example);

    int deleteByExample(DistrictExample example);

    int deleteByPrimaryKey(String id);

    int insert(District row);

    int insertSelective(District row);

    List<District> selectByExample(DistrictExample example);

    District selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") District row, @Param("example") DistrictExample example);

    int updateByExample(@Param("row") District row, @Param("example") DistrictExample example);

    int updateByPrimaryKeySelective(District row);

    int updateByPrimaryKey(District row);
}