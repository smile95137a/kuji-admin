package com.group.admin.mapper;

import com.group.admin.entity.PaymentResponse;
import com.group.admin.example.PaymentResponseExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface PaymentResponseMapper {

    int deleteByPrimaryKey(@Param("id") String id);

    int insert(PaymentResponse row);

    PaymentResponse selectByPrimaryKey(@Param("id") String id);

    List<PaymentResponse> selectAll();

    int updateByPrimaryKey(PaymentResponse row);

    List<PaymentResponse> selectByExample(PaymentResponseExample example);

    long countByExample(PaymentResponseExample example);

    int deleteByExample(PaymentResponseExample example);

}
