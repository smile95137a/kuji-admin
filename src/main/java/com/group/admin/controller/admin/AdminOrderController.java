package com.group.admin.controller.admin;

import com.group.admin.condition.OrderCondition;
import com.group.admin.req.common.QueryReq;
import com.group.admin.req.order.OrderCancelReq;
import com.group.admin.req.order.OrderShipReq;
import com.group.admin.req.order.UpdateOrderStatusReq;
import com.group.admin.res.order.OrderDetailRes;
import com.group.admin.res.order.OrderRes;
import com.group.admin.service.OrderService;
import com.group.admin.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 後台訂單管理 Controller
 */
@Slf4j
@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
public class AdminOrderController {

    private final OrderService orderService;

    /**
     * 查詢訂單列表（支援多條件查詢）
     * STORE_OWNER / STORE_EDITOR 只能查看自己店家的訂單
     */
    @PostMapping("/list")
    public ResponseEntity<List<OrderRes>> getOrders(
            @RequestBody(required = false) QueryReq<OrderCondition> req) {

        if (req == null) req = new QueryReq<>();
        if (req.getCondition() == null) req.setCondition(new OrderCondition());

        // Auto-inject storeId for store roles
        if (!SecurityUtils.isAdmin()) {
            String storeId = SecurityUtils.getCurrentUserPrimaryStoreId();
            if (storeId != null) {
                req.getCondition().setStoreId(storeId);
                log.info("🏪 [Admin] 店家訂單查詢：storeId={}", storeId);
            }
        }

        log.info("🔍 [Admin] 查詢訂單列表");
        return ResponseEntity.ok(orderService.getOrders(req));
    }

    /**
     * 查詢訂單詳情
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailRes> getOrderDetail(@PathVariable String orderId) {
        log.info("🔍 [Admin] 查詢訂單詳情：orderId={}", orderId);
        OrderDetailRes order = orderService.getOrderDetail(orderId);

        // Store isolation: non-admin can only view their store's orders
        if (!SecurityUtils.isAdmin()) {
            String storeId = SecurityUtils.getCurrentUserPrimaryStoreId();
            if (storeId != null && !storeId.equals(order.getStoreId())) {
                log.warn("⚠️ 無權查看此訂單：orderId={}", orderId);
                return ResponseEntity.status(403).build();
            }
        }
        return ResponseEntity.ok(order);
    }

    /**
     * 統一更新訂單狀態（PENDING→PREPARING→SHIPPED→COMPLETED）
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderRes> updateStatus(
            @PathVariable String orderId,
            @Valid @RequestBody UpdateOrderStatusReq req) {
        String operatorId = SecurityUtils.getCurrentAdminUserId();
        String operatorType = SecurityUtils.isAdmin() ? "ADMIN" : "STORE_OWNER";
        log.info("🔄 [Admin] 更新訂單狀態：orderId={}, target={}", orderId, req.getTargetStatus());
        OrderRes result = orderService.updateOrderStatus(orderId, req, operatorId, operatorType);
        return ResponseEntity.ok(result);
    }

    /**
     * 取消訂單（PENDING 或 PREPARING）
     */
    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
    public ResponseEntity<OrderRes> cancelOrder(
            @PathVariable String orderId,
            @RequestBody(required = false) OrderCancelReq req) {
        String operatorId = SecurityUtils.getCurrentAdminUserId();
        String operatorType = SecurityUtils.isAdmin() ? "ADMIN" : "STORE_OWNER";
        log.info("🚫 [Admin] 取消訂單：orderId={}", orderId);
        OrderRes result = orderService.cancelOrder(orderId, req, operatorId, operatorType);
        return ResponseEntity.ok(result);
    }

    // ─── Legacy endpoints (backward compat) ──────────────────────────────────

    @PutMapping("/{orderId}/prepare")
    public ResponseEntity<Void> prepareShipping(@PathVariable String orderId) {
        String operatorId = SecurityUtils.getCurrentAdminUserId();
        orderService.prepareShipping(orderId, operatorId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{orderId}/ship")
    public ResponseEntity<Void> ship(
            @PathVariable String orderId,
            @Valid @RequestBody OrderShipReq req) {
        String operatorId = SecurityUtils.getCurrentAdminUserId();
        orderService.ship(orderId, req, operatorId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{orderId}/complete")
    public ResponseEntity<Void> complete(@PathVariable String orderId) {
        String operatorId = SecurityUtils.getCurrentAdminUserId();
        orderService.complete(orderId, operatorId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{orderId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
    public ResponseEntity<Void> cancelLegacy(
            @PathVariable String orderId,
            @RequestBody(required = false) OrderCancelReq req) {
        String operatorId = SecurityUtils.getCurrentAdminUserId();
        orderService.cancel(orderId, req, operatorId);
        return ResponseEntity.ok().build();
    }
}
