package com.group.admin.service;

import com.group.admin.condition.OrderCondition;
import com.group.admin.req.common.QueryReq;
import com.group.admin.req.order.CancelOrderReq;
import com.group.admin.req.order.CreateOrderReq;
import com.group.admin.req.order.OrderCancelReq;
import com.group.admin.req.order.OrderShipReq;
import com.group.admin.req.order.UpdateOrderStatusReq;
import com.group.admin.res.PageResult;
import com.group.admin.res.order.OrderDetailRes;
import com.group.admin.res.order.OrderPaymentInitRes;
import com.group.admin.res.order.OrderRes;
import com.group.admin.gateway.ShippingCallbackResult;
import com.group.admin.service.logistics.ShippingResult;

import java.util.List;

public interface OrderService {

    List<String> createOrdersFromPrizeBox(String userId, List<String> prizeBoxIds,
                                          String shippingMethod, String recipientName,
                                          String recipientPhone, String recipientAddress,
                                          String storeCode, String storeName, String storeAddress);

    List<String> createOrdersFromPrizeBox(String userId, CreateOrderReq req);

    List<OrderPaymentInitRes> createOrdersFromPrizeBoxWithPayment(String userId, CreateOrderReq req);

    OrderPaymentInitRes retryShippingPayment(String orderId, String userId, String paymentMethod);

    List<OrderPaymentInitRes> getOrdersByPaymentGroup(String merchantOrderNo, String userId);

    PageResult<OrderRes> getOrders(QueryReq<OrderCondition> req);

    PageResult<OrderRes> getOrderList(QueryReq<OrderCondition> req, String callerUserId, String callerRole);

    OrderDetailRes getOrderDetail(String orderId);

    OrderDetailRes getOrderById(String id, String callerUserId, String callerRole);

    PageResult<OrderRes> getPlayerOrderList(QueryReq<OrderCondition> req, String playerId);

    OrderDetailRes getPlayerOrderById(String orderId, String playerId);

    void prepareShipping(String orderId, String operatorId);

    ShippingResult createShipment(String orderId, String operatorId);

    void ship(String orderId, OrderShipReq req, String operatorId);

    void complete(String orderId, String operatorId);

    void cancel(String orderId, OrderCancelReq req, String operatorId);

    void updateOrderStatus(String id, UpdateOrderStatusReq req, String operatorId, String operatorType);

    void cancelOrder(String id, CancelOrderReq req, String operatorId, String operatorType);

    // ========== 相容方法 ==========

    default List<String> createOrder(String userId, CreateOrderReq req) {
        return createOrdersFromPrizeBox(userId, req);
    }

    OrderRes cancelOrder(String id, OrderCancelReq req, String operatorId, String operatorType);

    void handleShippingPaymentCallback(ShippingCallbackResult callbackResult);

    void submitShippingInfo(String orderId, com.group.admin.req.order.ShipInfoReq req, String userId);

    java.util.List<com.group.admin.res.order.StatusLogRes> getStatusLog(String orderId);
}
