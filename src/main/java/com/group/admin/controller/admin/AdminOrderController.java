package com.group.admin.controller.admin;

import com.group.admin.condition.OrderCondition;
import com.group.admin.req.common.QueryReq;
import com.group.admin.req.order.CancelOrderReq;
import com.group.admin.req.order.OrderCancelReq;
import com.group.admin.req.order.OrderShipReq;
import com.group.admin.req.order.UpdateOrderStatusReq;
import com.group.admin.res.order.OrderDetailRes;
import com.group.admin.res.order.OrderRes;
import com.group.admin.res.order.StatusLogRes;
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
 * 
 * @author Kuji Admin
 * @since 2026-01-09
 */
@Slf4j
@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
public class AdminOrderController {
    
    private final OrderService orderService;
    
    /**
     * 查詢訂單列表（支援多條件查詢，角色自動限定店家範圍）
     */
    @PostMapping("/list")
    public ResponseEntity<List<OrderRes>> getOrders(
            @RequestBody(required = false) QueryReq<OrderCondition> req) {
        String currentUserId = SecurityUtils.getCurrentAdminUserId();
        String callerRole = resolveCallerRole();
        log.info("🔍 [Admin] 查詢訂單列表：userId={}, role={}", currentUserId, callerRole);
        
        List<OrderRes> orders = orderService.getOrderList(req, currentUserId, callerRole);
        
        return ResponseEntity.ok(orders);
    }
    
    /**
     * 查詢訂單詳情（含權限檢查）
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailRes> getOrderDetail(@PathVariable String orderId) {
        String currentUserId = SecurityUtils.getCurrentAdminUserId();
        String callerRole = resolveCallerRole();
        log.info("🔍 [Admin] 查詢訂單詳情：orderId={}, role={}", orderId, callerRole);
        
        OrderDetailRes order = orderService.getOrderById(orderId, currentUserId, callerRole);
        return ResponseEntity.ok(order);
    }
    
    /**
     * 統一更新訂單狀態（狀態機驗證）
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<Void> updateOrderStatus(
            @PathVariable String orderId,
            @Valid @RequestBody UpdateOrderStatusReq req) {
        String operatorId = SecurityUtils.getCurrentAdminUserId();
        log.info("🔍 [Admin] 更新訂單狀態：orderId={}, target={}, operator={}", 
                orderId, req.getTargetStatus(), operatorId);
        
        orderService.updateOrderStatus(orderId, req, operatorId, "ADMIN");
        
        return ResponseEntity.ok().build();
    }
    
    /**
     * 準備出貨（店家確認備貨完成）
     */
    @PutMapping("/{orderId}/prepare")
    public ResponseEntity<Void> prepareShipping(@PathVariable String orderId) {
        String operatorId = SecurityUtils.getCurrentAdminUserId();
        log.info("🔍 [Admin] 準備出貨：orderId={}, operator={}", orderId, operatorId);
        
        orderService.prepareShipping(orderId, operatorId);
        
        return ResponseEntity.ok().build();
    }
    
    /**
     * 訂單出貨（填寫物流單號）
     */
    @PutMapping("/{orderId}/ship")
    public ResponseEntity<Void> ship(
            @PathVariable String orderId,
            @Valid @RequestBody OrderShipReq req) {
        String operatorId = SecurityUtils.getCurrentAdminUserId();
        log.info("🔍 [Admin] 訂單出貨：orderId={}, trackingNo={}, operator={}", 
                orderId, req.getTrackingNo(), operatorId);
        
        orderService.ship(orderId, req, operatorId);
        
        return ResponseEntity.ok().build();
    }
    
    /**
     * 完成訂單
     */
    @PutMapping("/{orderId}/complete")
    public ResponseEntity<Void> complete(@PathVariable String orderId) {
        String operatorId = SecurityUtils.getCurrentAdminUserId();
        log.info("🔍 [Admin] 完成訂單：orderId={}, operator={}", orderId, operatorId);
        
        orderService.complete(orderId, operatorId);
        
        return ResponseEntity.ok().build();
    }
    
    /**
     * 取消訂單
     */
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> cancelOrder(
            @PathVariable String orderId,
            @RequestBody(required = false) CancelOrderReq req) {
        String operatorId = SecurityUtils.getCurrentAdminUserId();
        log.info("🔍 [Admin] 取消訂單：orderId={}, operator={}", orderId, operatorId);
        
        orderService.cancelOrder(orderId, req, operatorId, "ADMIN");
        
        return ResponseEntity.ok().build();
    }
    
    private String resolveCallerRole() {
        if (SecurityUtils.isAdmin()) {
            return "ROLE_ADMIN";
        } else if (SecurityUtils.isStoreOwner()) {
            return "ROLE_STORE_OWNER";
        } else if (SecurityUtils.isStoreEditor()) {
            return "ROLE_STORE_EDITOR";
        }
        return "UNKNOWN";
    }

    /**
     * 查詢訂單狀態歷史記錄
     */
    @GetMapping("/{orderId}/status-log")
    public ResponseEntity<List<StatusLogRes>> getStatusLog(@PathVariable String orderId) {
        log.info("🔍 [Admin] 查詢訂單狀態歷史：orderId={}", orderId);
        return ResponseEntity.ok(orderService.getStatusLog(orderId));
    }
}
