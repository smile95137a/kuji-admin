package com.group.admin.mapper;

import com.group.admin.entity.PaymentRecord;
import com.group.admin.example.PaymentRecordExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface PaymentRecordMapper {

    int deleteByPrimaryKey(@Param("id") Long id);

    int insert(PaymentRecord row);

    PaymentRecord selectByPrimaryKey(@Param("id") Long id);

    List<PaymentRecord> selectAll();

    int updateByPrimaryKey(PaymentRecord row);

    List<PaymentRecord> selectByExample(PaymentRecordExample example);

    long countByExample(PaymentRecordExample example);

    int deleteByExample(PaymentRecordExample example);

}
