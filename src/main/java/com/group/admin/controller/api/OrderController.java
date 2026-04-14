package com.group.admin.controller.api;

import com.group.admin.condition.OrderCondition;
import com.group.admin.req.common.QueryReq;
import com.group.admin.req.order.CreateOrderReq;
import com.group.admin.req.order.OrderCancelReq;
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
 */
@Slf4j
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * US3 - 從獎品盒建立出貨訂單
     * 驗證所有 prizeBoxIds 屬於當前使用者且狀態為 IN_BOX，按店家自動拆單
     */
    @PostMapping("/ship")
    public ResponseEntity<List<String>> createOrder(@Valid @RequestBody CreateOrderReq req) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("🛒 [API] 建立訂單：userId={}, prizeBoxCount={}", userId, req.getPrizeBoxIds().size());
        List<String> orderIds = orderService.createOrder(userId, req);
        return ResponseEntity.ok(orderIds);
    }

    /**
     * US2 - 查詢我的訂單列表
     */
    @PostMapping("/list")
    public ResponseEntity<List<OrderRes>> getMyOrders(
            @RequestBody(required = false) QueryReq<OrderCondition> req) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("🔍 [API] 查詢我的訂單：userId={}", userId);

        if (req == null) req = new QueryReq<>();
        if (req.getCondition() == null) req.setCondition(new OrderCondition());
        req.getCondition().setUserId(userId);

        return ResponseEntity.ok(orderService.getOrders(req));
    }

    /**
     * US2 - 查詢我的訂單詳情
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailRes> getOrderDetail(@PathVariable String orderId) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("🔍 [API] 查詢訂單詳情：userId={}, orderId={}", userId, orderId);

        OrderDetailRes order = orderService.getOrderDetail(orderId);

        // 玩家只能查看自己的訂單
        if (!userId.equals(order.getUserId())) {
            log.warn("⚠️ 無權查看此訂單：userId={}, orderId={}", userId, orderId);
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(order);
    }

    /**
     * US4 - 玩家取消訂單（僅限 PENDING 狀態）
     */
    @DeleteMapping("/{orderId}/cancel")
    public ResponseEntity<OrderRes> cancelOrder(
            @PathVariable String orderId,
            @RequestBody(required = false) OrderCancelReq req) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("🚫 [API] 玩家取消訂單：userId={}, orderId={}", userId, orderId);

        // Verify order belongs to this user
        OrderDetailRes order = orderService.getOrderDetail(orderId);
        if (!userId.equals(order.getUserId())) {
            log.warn("⚠️ 無權取消此訂單：userId={}, orderId={}", userId, orderId);
            return ResponseEntity.status(403).build();
        }

        OrderRes result = orderService.cancelOrder(orderId, req, userId, "PLAYER");
        return ResponseEntity.ok(result);
    }
}
