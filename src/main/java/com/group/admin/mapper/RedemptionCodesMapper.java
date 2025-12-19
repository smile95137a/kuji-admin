package com.group.admin.mapper;

import com.group.admin.entity.RedemptionCodes;
import com.group.admin.example.RedemptionCodesExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface RedemptionCodesMapper {

    int deleteByPrimaryKey(@Param("id") Long id);

    int insert(RedemptionCodes row);

    RedemptionCodes selectByPrimaryKey(@Param("id") Long id);

    List<RedemptionCodes> selectAll();

    int updateByPrimaryKey(RedemptionCodes row);

    List<RedemptionCodes> selectByExample(RedemptionCodesExample example);

    long countByExample(RedemptionCodesExample example);

    int deleteByExample(RedemptionCodesExample example);

}
