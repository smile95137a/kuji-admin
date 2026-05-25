package com.group.admin.mapper;

import com.group.admin.entity.RechargeOrder;
import com.group.admin.enums.RechargeOrderStatus;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RechargeOrderMapper {
    int insert(RechargeOrder order);
    RechargeOrder selectById(String id);
    /** CAS update: only update if current status == expectedStatus. Returns affected rows. */
    int updateStatusByIdAndExpectStatus(@Param("id") String id,
                                        @Param("newStatus") RechargeOrderStatus newStatus,
                                        @Param("expectedStatus") RechargeOrderStatus expectedStatus,
                                        @Param("gatewayOrderId") String gatewayOrderId,
                                        @Param("gatewayRawResp") String gatewayRawResp,
                                        @Param("paidAt") LocalDateTime paidAt);
    /** Batch expire: PENDING orders past expired_at → EXPIRED */
    int updateExpiredOrders(@Param("now") LocalDateTime now);
    List<RechargeOrder> selectExpiredPendingOrders(@Param("now") LocalDateTime now);
    int updateGatewayInit(RechargeOrder order);
    List<RechargeOrder> selectByUserId(String userId);
}
