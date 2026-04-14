package com.group.admin.controller.api;

import com.group.admin.condition.OrderCondition;
import com.group.admin.exception.BusinessException;
import com.group.admin.req.common.QueryReq;
import com.group.admin.res.order.OrderDetailRes;
import com.group.admin.res.order.OrderRes;
import com.group.admin.service.OrderService;
import com.group.admin.util.SecurityUtils;
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
     * 查詢我的訂單列表
     */
    @PostMapping("/list")
    public ResponseEntity<List<OrderRes>> getMyOrders(
            @RequestBody(required = false) QueryReq<OrderCondition> req) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("🔍 [API] 查詢我的訂單：userId={}", userId);
        
        // 強制設定為當前玩家
        if (req == null) {
            req = new QueryReq<>();
        }
        if (req.getCondition() == null) {
            req.setCondition(new OrderCondition());
        }
        req.getCondition().setUserId(userId);
        
        List<OrderRes> orders = orderService.getOrders(req);
        
        return ResponseEntity.ok(orders);
    }
    
    /**
     * 查詢訂單詳情
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailRes> getOrderDetail(@PathVariable String orderId) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("🔍 [API] 查詢訂單詳情：userId={}, orderId={}", userId, orderId);
        
        OrderDetailRes order = orderService.getOrderDetail(orderId);
        
        // 驗證訂單屬於當前玩家
        if (!order.getUserId().equals(userId)) {
            log.warn("⚠️ 無權查看此訂單：userId={}, orderId={}", userId, orderId);
            return ResponseEntity.status(403).build();
        }
        
        return ResponseEntity.ok(order);
    }

    /**
     * 查詢我的訂單列表（GET 方式，通過查詢參數篩選）
     */
    @GetMapping("/my")
    public ResponseEntity<List<OrderRes>> getMyOrderList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("🔍 [API] 查詢我的訂單列表（GET）：userId={}, status={}", userId, status);

        QueryReq<OrderCondition> req = new QueryReq<>();
        OrderCondition condition = new OrderCondition();
        condition.setUserId(userId);
        if (status != null && !status.isEmpty()) {
            condition.setShippingStatus(status);
        }
        req.setCondition(condition);

        List<OrderRes> orders = orderService.getOrders(req);
        return ResponseEntity.ok(orders);
    }

    /**
     * 查詢我的訂單詳情（確保只能查看自己的訂單，404 優先）
     */
    @GetMapping("/my/{orderId}")
    public ResponseEntity<OrderDetailRes> getMyOrderDetail(@PathVariable String orderId) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("🔍 [API] 查詢我的訂單詳情：userId={}, orderId={}", userId, orderId);

        OrderDetailRes order;
        try {
            order = orderService.getOrderDetail(orderId);
        } catch (BusinessException e) {
            return ResponseEntity.notFound().build();
        }

        if (!order.getUserId().equals(userId)) {
            log.warn("⚠️ 無權查看此訂單：userId={}, orderId={}", userId, orderId);
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(order);
    }
}
