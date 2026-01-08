package com.group.admin.controller.admin;

import com.group.admin.condition.OrderCondition;
import com.group.admin.req.common.QueryReq;
import com.group.admin.req.order.OrderCancelReq;
import com.group.admin.req.order.OrderShipReq;
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
 * 
 * @author Kuji Admin
 * @since 2026-01-09
 */
@Slf4j
@RestController
@RequestMapping("/admin/order")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
public class AdminOrderController {
    
    private final OrderService orderService;
    
    /**
     * 查詢訂單列表（支援多條件查詢）
     */
    @PostMapping("/list")
    public ResponseEntity<List<OrderRes>> getOrders(
            @RequestBody(required = false) QueryReq<OrderCondition> req) {
        log.info("🔍 [Admin] 查詢訂單列表");
        
        // 店家只能查看自己的訂單
        String currentUserId = SecurityUtils.getCurrentAdminUserId();
        if (SecurityUtils.hasRole("ROLE_STORE_OWNER")) {
            // TODO: 從 store_user 查詢店家 ID，設定到 condition
            log.info("店家負責人查詢：userId={}", currentUserId);
        }
        
        List<OrderRes> orders = orderService.getOrders(req);
        
        return ResponseEntity.ok(orders);
    }
    
    /**
     * 查詢訂單詳情
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailRes> getOrderDetail(@PathVariable String orderId) {
        log.info("🔍 [Admin] 查詢訂單詳情：orderId={}", orderId);
        OrderDetailRes order = orderService.getOrderDetail(orderId);
        return ResponseEntity.ok(order);
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
     * 取消訂單（僅限 PENDING 狀態）
     */
    @PutMapping("/{orderId}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cancel(
            @PathVariable String orderId,
            @Valid @RequestBody OrderCancelReq req) {
        String operatorId = SecurityUtils.getCurrentAdminUserId();
        log.info("🔍 [Admin] 取消訂單：orderId={}, reason={}, operator={}", 
                orderId, req.getReason(), operatorId);
        
        orderService.cancel(orderId, req, operatorId);
        
        return ResponseEntity.ok().build();
    }
}
