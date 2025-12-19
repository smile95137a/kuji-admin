package com.group.admin.mapper;

import com.group.admin.entity.PaymentCallbackLog;
import com.group.admin.example.PaymentCallbackLogExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface PaymentCallbackLogMapper {

    int deleteByPrimaryKey(@Param("id") Long id);

    int insert(PaymentCallbackLog row);

    PaymentCallbackLog selectByPrimaryKey(@Param("id") Long id);

    List<PaymentCallbackLog> selectAll();

    int updateByPrimaryKey(PaymentCallbackLog row);

    List<PaymentCallbackLog> selectByExample(PaymentCallbackLogExample example);

    long countByExample(PaymentCallbackLogExample example);

    int deleteByExample(PaymentCallbackLogExample example);

}
