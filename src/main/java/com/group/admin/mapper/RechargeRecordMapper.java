package com.group.admin.mapper;

import com.group.admin.entity.RechargeRecord;
import com.group.admin.example.RechargeRecordExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface RechargeRecordMapper {
    long countByExample(RechargeRecordExample example);

    int deleteByExample(RechargeRecordExample example);

    int deleteByPrimaryKey(String id);

    int insert(RechargeRecord row);

    int insertSelective(RechargeRecord row);

    List<RechargeRecord> selectByExampleWithBLOBs(RechargeRecordExample example);

    List<RechargeRecord> selectByExample(RechargeRecordExample example);

    long countByUserId(@Param("userId") String userId);

    List<RechargeRecord> selectByUserIdPaged(@Param("userId") String userId,
                                             @Param("offset") int offset,
                                             @Param("limit") int limit);

    RechargeRecord selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") RechargeRecord row, @Param("example") RechargeRecordExample example);

    int updateByExampleWithBLOBs(@Param("row") RechargeRecord row, @Param("example") RechargeRecordExample example);

    int updateByExample(@Param("row") RechargeRecord row, @Param("example") RechargeRecordExample example);

    int updateByPrimaryKeySelective(RechargeRecord row);

    int updateByPrimaryKeyWithBLOBs(RechargeRecord row);

    int updateByPrimaryKey(RechargeRecord row);
}
