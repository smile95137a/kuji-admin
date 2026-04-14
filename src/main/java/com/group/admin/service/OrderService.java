package com.group.admin.service;

import com.group.admin.condition.OrderCondition;
import com.group.admin.req.common.QueryReq;
import com.group.admin.req.order.OrderCancelReq;
import com.group.admin.req.order.OrderShipReq;
import com.group.admin.req.order.ShipInfoReq;
import com.group.admin.res.order.OrderDetailRes;
import com.group.admin.res.order.OrderRes;

import java.util.List;

/**
 * 訂單服務介面
 * 管理從賞品盒產生的出貨訂單
 * 
 * @author Kuji Admin
 * @since 2026-01-09
 */
public interface OrderService {
    
    /**
     * 從賞品盒產生訂單（由 PrizeBoxService 呼叫）
     * 會自動按店家拆分訂單
     * 
     * @param userId 玩家 ID
     * @param prizeBoxIds 賞品盒 ID 列表
     * @param shippingMethod 配送方式
     * @param recipientName 收件人姓名
     * @param recipientPhone 收件人電話
     * @param recipientAddress 收件地址（宅配）
     * @param storeCode 超商店號（超商取貨）
     * @param storeName 超商店名（超商取貨）
     * @param storeAddress 超商地址（超商取貨）
     * @return 訂單 ID 列表
     */
    List<String> createOrdersFromPrizeBox(String userId, List<String> prizeBoxIds, 
                                          String shippingMethod, String recipientName, 
                                          String recipientPhone, String recipientAddress, 
                                          String storeCode, String storeName, String storeAddress);
    
    /**
     * 查詢訂單列表（支援多條件查詢）
     * 
     * @param req 查詢條件
     * @return 訂單列表（精簡版）
     */
    List<OrderRes> getOrders(QueryReq<OrderCondition> req);
    
    /**
     * 取得訂單詳情（完整版，包含所有項目）
     * 
     * @param orderId 訂單 ID
     * @return 訂單詳情
     */
    OrderDetailRes getOrderDetail(String orderId);
    
    /**
     * 準備出貨（店家確認備貨完成）
     * 
     * @param orderId 訂單 ID
     * @param operatorId 操作者 ID
     */
    void prepareShipping(String orderId, String operatorId);
    
    /**
     * 出貨（填寫物流單號）
     * 
     * @param orderId 訂單 ID
     * @param req 出貨請求（包含物流單號）
     * @param operatorId 操作者 ID
     */
    void ship(String orderId, OrderShipReq req, String operatorId);
    
    /**
     * 完成訂單（玩家確認收貨或自動完成）
     * 
     * @param orderId 訂單 ID
     * @param operatorId 操作者 ID（可為 null，表示自動完成）
     */
    void complete(String orderId, String operatorId);
    
    /**
     * 取消訂單（僅限 PENDING 狀態）
     * 
     * @param orderId 訂單 ID
     * @param req 取消請求（包含取消原因）
     * @param operatorId 操作者 ID
     */
    void cancel(String orderId, OrderCancelReq req, String operatorId);

    /**
     * 玩家提交或更新出貨資訊（僅限 PENDING 狀態）
     *
     * @param orderId 訂單 ID
     * @param req     出貨資訊請求（配送方式及收件人資訊）
     * @param userId  已認證的玩家 ID（用於所有權驗證）
     */
    void submitShippingInfo(String orderId, ShipInfoReq req, String userId);
}
