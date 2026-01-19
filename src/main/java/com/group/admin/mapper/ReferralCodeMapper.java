package com.group.admin.mapper;

import com.group.admin.entity.ReferralCode;
import com.group.admin.example.ReferralCodeExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ReferralCodeMapper {
    long countByExample(ReferralCodeExample example);

    int deleteByExample(ReferralCodeExample example);

    int deleteByPrimaryKey(String id);

    int insert(ReferralCode row);

    int insertSelective(ReferralCode row);

    List<ReferralCode> selectByExample(ReferralCodeExample example);

    ReferralCode selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") ReferralCode row, @Param("example") ReferralCodeExample example);

    int updateByExample(@Param("row") ReferralCode row, @Param("example") ReferralCodeExample example);

    int updateByPrimaryKeySelective(ReferralCode row);

    int updateByPrimaryKey(ReferralCode row);
}