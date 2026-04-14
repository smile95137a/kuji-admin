package com.group.admin.service;

import com.group.admin.condition.OrderCondition;
import com.group.admin.req.common.QueryReq;
import com.group.admin.req.order.CreateOrderReq;
import com.group.admin.req.order.OrderCancelReq;
import com.group.admin.req.order.OrderShipReq;
import com.group.admin.req.order.UpdateOrderStatusReq;
import com.group.admin.res.order.OrderDetailRes;
import com.group.admin.res.order.OrderRes;

import java.util.List;

/**
 * 訂單服務介面
 */
public interface OrderService {

    /**
     * 從獎品盒建立訂單（供 PrizeBoxService 呼叫）
     */
    List<String> createOrdersFromPrizeBox(String userId, List<String> prizeBoxIds,
                                          String shippingMethod, String recipientName,
                                          String recipientPhone, String recipientAddress,
                                          String storeCode, String storeName, String storeAddress);

    /**
     * 從獎品盒建立訂單（玩家前台 POST /order/ship）
     */
    List<String> createOrder(String userId, CreateOrderReq req);

    /**
     * 查詢訂單列表
     */
    List<OrderRes> getOrders(QueryReq<OrderCondition> req);

    /**
     * 取得訂單詳情
     */
    OrderDetailRes getOrderDetail(String orderId);

    /**
     * 統一更新訂單狀態（PENDING→PREPARING→SHIPPED→COMPLETED）
     * 
     * @param operatorType ADMIN / STORE_OWNER / STORE_EDITOR / PLAYER / SYSTEM
     */
    OrderRes updateOrderStatus(String orderId, UpdateOrderStatusReq req,
                               String operatorId, String operatorType);

    /**
     * 取消訂單（管理端）
     */
    OrderRes cancelOrder(String orderId, OrderCancelReq req,
                         String operatorId, String operatorType);

    // --- legacy methods kept for backward compatibility ---

    void prepareShipping(String orderId, String operatorId);

    void ship(String orderId, OrderShipReq req, String operatorId);

    void complete(String orderId, String operatorId);

    void cancel(String orderId, OrderCancelReq req, String operatorId);
}
