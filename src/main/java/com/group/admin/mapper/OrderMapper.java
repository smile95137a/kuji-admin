package com.group.admin.mapper;

import com.group.admin.condition.OrderCondition;
import com.group.admin.entity.Order;
import com.group.admin.example.OrderExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface OrderMapper {
    long countByExample(OrderExample example);

    int deleteByExample(OrderExample example);

    int deleteByPrimaryKey(String id);

    int insert(Order row);

    int insertSelective(Order row);

    List<Order> selectByExample(OrderExample example);

    Order selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("row") Order row, @Param("example") OrderExample example);

    int updateByExample(@Param("row") Order row, @Param("example") OrderExample example);

    int updateByPrimaryKeySelective(Order row);

    int updateByPrimaryKey(Order row);

    // ==================== Custom methods ====================

    List<Order> selectByCondition(@Param("condition") OrderCondition condition);

    List<Order> selectByConditionPaged(@Param("condition") OrderCondition condition,
                                       @Param("offset") int offset,
                                       @Param("limit") int limit);

    long countByCondition(@Param("condition") OrderCondition condition);

    Order selectByOrderNumber(@Param("orderNumber") String orderNumber);

    int updatePaymentInit(@Param("orderId") String orderId,
                          @Param("shippingFee") Long shippingFee,
                          @Param("paymentMethod") String paymentMethod,
                          @Param("paymentStatus") String paymentStatus,
                          @Param("status") String status,
                          @Param("gomypayTradeNo") String gomypayTradeNo);

    int markShippingPaymentSuccess(@Param("orderId") String orderId,
                                   @Param("gomypayTradeNo") String gomypayTradeNo);

    int markShippingPaymentFailed(@Param("orderId") String orderId,
                                  @Param("gomypayTradeNo") String gomypayTradeNo,
                                  @Param("remark") String remark);
}
