package com.group.admin.mapper;

import com.group.admin.entity.ReferralRecord;
import com.group.admin.example.ReferralRecordExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ReferralRecordMapper {
    long countByExample(ReferralRecordExample example);

    int deleteByExample(ReferralRecordExample example);

    int deleteByPrimaryKey(String id);

    int insert(ReferralRecord row);

    int insertSelective(ReferralRecord row);

    List<ReferralRecord> selectByExample(ReferralRecordExample example);

    ReferralRecord selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") ReferralRecord row, @Param("example") ReferralRecordExample example);

    int updateByExample(@Param("row") ReferralRecord row, @Param("example") ReferralRecordExample example);

    int updateByPrimaryKeySelective(ReferralRecord row);

    int updateByPrimaryKey(ReferralRecord row);

    // ========== Custom methods ==========

    List<ReferralRecord> selectByReferrerId(@Param("referrerId") String referrerId,
                                            @Param("offset") int offset,
                                            @Param("limit") int limit);

    long countByReferrerId(@Param("referrerId") String referrerId);
}