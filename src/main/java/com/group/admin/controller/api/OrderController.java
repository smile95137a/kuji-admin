package com.group.admin.controller.api;

import com.group.admin.condition.OrderCondition;
import com.group.admin.req.common.QueryReq;
import com.group.admin.req.order.CreateOrderReq;
import com.group.admin.req.order.ShipInfoReq;
import com.group.admin.res.order.OrderDetailRes;
import com.group.admin.res.order.OrderRes;
import com.group.admin.service.OrderService;
import com.group.admin.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 前台訂單 API
 * 
 * @author Kuji Admin
 * @since 2026-01-09
 */
@Slf4j
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;

    /**
     * 建立訂單（從賞品盒出貨）
     */
    @PostMapping("/create")
    public ResponseEntity<List<String>> createOrder(@Valid @RequestBody CreateOrderReq req) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("📦 [API] 建立訂單：userId={}, prizeBoxCount={}", userId, req.getPrizeBoxIds().size());

        List<String> orderIds = orderService.createOrdersFromPrizeBox(userId, req);

        return ResponseEntity.ok(orderIds);
    }

    /**
     * 查詢我的訂單列表
     */
    @PostMapping("/list")
    public ResponseEntity<List<OrderRes>> getMyOrders(
            @RequestBody(required = false) QueryReq<OrderCondition> req) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("🔍 [API] 查詢我的訂單：userId={}", userId);
        
        List<OrderRes> orders = orderService.getPlayerOrderList(req, userId);
        
        return ResponseEntity.ok(orders);
    }
    
    /**
     * 查詢訂單詳情（含所有權驗證）
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailRes> getOrderDetail(@PathVariable String orderId) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("🔍 [API] 查詢訂單詳情：userId={}, orderId={}", userId, orderId);
        
        OrderDetailRes order = orderService.getPlayerOrderById(orderId, userId);
        
        return ResponseEntity.ok(order);
    }

    /**
     * 提交出貨資訊
     */
    @PostMapping("/{orderId}/shipping-info")
    public ResponseEntity<String> submitShippingInfo(
            @PathVariable String orderId,
            @Valid @RequestBody ShipInfoReq req) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("📦 [API] 提交出貨資訊：userId={}, orderId={}", userId, orderId);
        orderService.submitShippingInfo(orderId, req, userId);
        return ResponseEntity.ok("出貨資訊已更新");
    }
    
    /**
     * 查詢我的訂單列表（GET 便利端點）
     */
    @GetMapping("/list")
    public ResponseEntity<List<OrderRes>> getMyOrdersByGet(
            @RequestParam(required = false) String status) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("🔍 [API] 查詢我的訂單（GET）：userId={}, status={}", userId, status);
        
        QueryReq<OrderCondition> req = new QueryReq<>();
        OrderCondition condition = new OrderCondition();
        if (status != null && !status.isBlank()) {
            condition.setShippingStatus(status);
        }
        req.setCondition(condition);
        
        List<OrderRes> orders = orderService.getPlayerOrderList(req, userId);
        return ResponseEntity.ok(orders);
    }
}