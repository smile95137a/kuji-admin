package com.group.admin.mapper;

import com.group.admin.entity.PaymentOverView;
import com.group.admin.example.PaymentOverViewExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface PaymentOverViewMapper {

    int deleteByPrimaryKey(@Param("id") Long id);

    int insert(PaymentOverView row);

    PaymentOverView selectByPrimaryKey(@Param("id") Long id);

    List<PaymentOverView> selectAll();

    int updateByPrimaryKey(PaymentOverView row);

    List<PaymentOverView> selectByExample(PaymentOverViewExample example);

    long countByExample(PaymentOverViewExample example);

    int deleteByExample(PaymentOverViewExample example);

}
