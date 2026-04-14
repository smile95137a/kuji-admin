package com.group.admin.service.impl;

import com.group.admin.condition.OrderCondition;
import com.group.admin.entity.*;
import com.group.admin.enums.OrderStatusEnum;
import com.group.admin.enums.PrizeBoxStatusEnum;
import com.group.admin.example.OrderExample;
import com.group.admin.example.OrderItemExample;
import com.group.admin.example.OrderStatusLogExample;
import com.group.admin.exception.BusinessException;
import com.group.admin.exception.ConflictException;
import com.group.admin.mapper.*;
import com.group.admin.repository.OrderRepository;
import com.group.admin.req.common.QueryReq;
import com.group.admin.req.order.CreateOrderReq;
import com.group.admin.req.order.OrderCancelReq;
import com.group.admin.req.order.OrderShipReq;
import com.group.admin.req.order.UpdateOrderStatusReq;
import com.group.admin.res.order.OrderDetailRes;
import com.group.admin.res.order.OrderItemRes;
import com.group.admin.res.order.OrderRes;
import com.group.admin.res.order.StatusLogRes;
import com.group.admin.service.ConsumptionRecordService;
import com.group.admin.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderStatusLogMapper orderStatusLogMapper;
    private final PrizeBoxMapper prizeBoxMapper;
    private final LotteryMapper lotteryMapper;
    private final LotteryPrizeMapper lotteryPrizeMapper;
    private final StoreMapper storeMapper;
    private final UserMapper userMapper;
    private final OrderRepository orderRepository;
    private final ConsumptionRecordService consumptionRecordService;

    private static final Long SHIPPING_FEE = 60L;

    // ─── US3: Create order from prize box ────────────────────────────────────

    @Override
    @Transactional
    public List<String> createOrder(String userId, CreateOrderReq req) {
        log.info("🛒 [US3] 玩家建立訂單：userId={}, prizeBoxCount={}", userId, req.getPrizeBoxIds().size());

        // Validate all prize boxes belong to user and are IN_BOX
        List<String> unavailableIds = new ArrayList<>();
        for (String boxId : req.getPrizeBoxIds()) {
            PrizeBox box = prizeBoxMapper.selectByPrimaryKey(boxId);
            if (box == null || !userId.equals(box.getUserId())
                    || !PrizeBoxStatusEnum.IN_BOX.getCode().equals(box.getStatus())) {
                unavailableIds.add(boxId);
            }
        }
        if (!unavailableIds.isEmpty()) {
            throw new BusinessException("PRIZE_BOX_UNAVAILABLE",
                    "部分賞品盒已出貨或已回收，無法建立訂單：" + unavailableIds);
        }

        return createOrdersFromPrizeBox(userId, req.getPrizeBoxIds(),
                req.getShippingMethod(), req.getRecipientName(), req.getRecipientPhone(),
                req.getRecipientAddress(), req.getStoreCode(), req.getStoreName(), req.getStoreAddress());
    }

    @Override
    @Transactional
    public List<String> createOrdersFromPrizeBox(String userId, List<String> prizeBoxIds,
            String shippingMethod, String recipientName,
            String recipientPhone, String recipientAddress,
            String storeCode, String storeName, String storeAddress) {
        log.info("🔍 從賞品盒建立訂單：userId={}, prizeBoxCount={}", userId, prizeBoxIds.size());

        List<PrizeBox> prizeBoxes = prizeBoxIds.stream()
                .map(prizeBoxMapper::selectByPrimaryKey)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Group by store
        Map<String, List<PrizeBox>> groupedByStore = prizeBoxes.stream()
                .collect(Collectors.groupingBy(PrizeBox::getStoreId));

        List<String> orderIds = new ArrayList<>();

        for (Map.Entry<String, List<PrizeBox>> entry : groupedByStore.entrySet()) {
            String storeId = entry.getKey();
            List<PrizeBox> storePrizeBoxes = entry.getValue();

            Order order = new Order();
            order.setId(UUID.randomUUID().toString());
            order.setOrderNumber(generateOrderNumber());
            order.setUserId(userId);
            order.setStoreId(storeId);
            order.setStatus(OrderStatusEnum.PENDING.getCode());
            order.setTotalItems(storePrizeBoxes.size());
            order.setShippingMethod(shippingMethod);
            order.setRecipientName(recipientName);
            order.setRecipientPhone(recipientPhone);
            order.setRecipientAddress(recipientAddress);
            order.setStoreCode(storeCode);
            order.setStoreName(storeName);
            order.setStoreAddress(storeAddress);
            order.setCreatedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());

            orderMapper.insert(order);

            // Create order items + update prize box status
            for (PrizeBox prizeBox : storePrizeBoxes) {
                Lottery lottery = lotteryMapper.selectByPrimaryKey(prizeBox.getLotteryId());
                LotteryPrize prize = lotteryPrizeMapper.selectByPrimaryKey(prizeBox.getPrizeId());

                OrderItem item = new OrderItem();
                item.setId(UUID.randomUUID().toString());
                item.setOrderId(order.getId());
                item.setPrizeBoxId(prizeBox.getId());
                item.setLotteryId(prizeBox.getLotteryId());
                item.setLotteryTitle(lottery != null ? lottery.getTitle() : "未知商品");
                item.setLotteryImageUrl(lottery != null ? lottery.getImageUrl() : null);
                item.setPrizeId(prizeBox.getPrizeId());
                item.setPrizeName(prize != null ? prize.getName() : "未知獎品");
                item.setPrizeImageUrl(prize != null ? prize.getImageUrl() : null);
                item.setPrizeLevel(prize != null ? prize.getLevel() : null);
                item.setCreatedAt(LocalDateTime.now());
                orderItemMapper.insert(item);

                // ✅ Update prize box: SHIPPED + set order_id
                prizeBox.setStatus(PrizeBoxStatusEnum.SHIPPED.getCode());
                prizeBox.setOrderId(order.getId());
                prizeBox.setShippedAt(LocalDateTime.now());
                prizeBoxMapper.updateByPrimaryKey(prizeBox);
            }

            // Log initial status
            insertStatusLog(order.getId(), null, OrderStatusEnum.PENDING.getCode(),
                    userId, "PLAYER", "訂單建立");

            // Record shipping fee consumption
            consumptionRecordService.recordConsumption(
                    userId, "SHIPPING_FEE", null, null,
                    order.getId(), order.getOrderNumber(),
                    SHIPPING_FEE, 0L,
                    String.format("訂單運費：%s（%s）", order.getOrderNumber(), shippingMethod));

            orderIds.add(order.getId());
        }

        log.info("✅ 訂單建立完成：orderCount={}", orderIds.size());
        return orderIds;
    }

    // ─── US1 / US4: List and detail ──────────────────────────────────────────

    @Override
    public List<OrderRes> getOrders(QueryReq<OrderCondition> req) {
        OrderCondition condition = req != null ? req.getCondition() : null;

        List<Order> orders = orderRepository.selectAll();

        if (condition != null) {
            orders = orders.stream().filter(order -> {
                if (isNotBlank(condition.getUserId())
                        && !condition.getUserId().equals(order.getUserId())) return false;
                if (isNotBlank(condition.getStoreId())
                        && !condition.getStoreId().equals(order.getStoreId())) return false;

                // Support both `status` and `shippingStatus` fields
                String statusFilter = isNotBlank(condition.getStatus())
                        ? condition.getStatus() : condition.getShippingStatus();
                if (isNotBlank(statusFilter)
                        && !statusFilter.equals(order.getStatus())) return false;

                if (isNotBlank(condition.getShippingMethod())
                        && !condition.getShippingMethod().equals(order.getShippingMethod())) return false;
                if (isNotBlank(condition.getOrderNo())
                        && !order.getOrderNumber().contains(condition.getOrderNo())) return false;
                if (condition.getCreatedAtStart() != null
                        && order.getCreatedAt().isBefore(condition.getCreatedAtStart().atStartOfDay())) return false;
                if (condition.getCreatedAtEnd() != null
                        && order.getCreatedAt().isAfter(condition.getCreatedAtEnd().atTime(23, 59, 59))) return false;
                return true;
            })
                    .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                    .collect(Collectors.toList());
        }

        return orders.stream().map(this::convertToRes).collect(Collectors.toList());
    }

    @Override
    public OrderDetailRes getOrderDetail(String orderId) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new BusinessException("NOT_FOUND", "訂單不存在");
        }

        OrderItemExample itemExample = new OrderItemExample();
        itemExample.createCriteria().andOrderIdEqualTo(orderId);
        List<OrderItem> items = orderItemMapper.selectByExample(itemExample);

        // Load status history
        OrderStatusLogExample logExample = new OrderStatusLogExample();
        logExample.createCriteria().andOrderIdEqualTo(orderId);
        logExample.setOrderByClause("created_at ASC");
        List<OrderStatusLog> logs = orderStatusLogMapper.selectByExample(logExample);

        User user = userMapper.selectByPrimaryKey(order.getUserId());
        Store store = storeMapper.selectByPrimaryKey(order.getStoreId());

        List<StatusLogRes> statusHistory = logs.stream()
                .map(this::convertLogToRes)
                .collect(Collectors.toList());

        return OrderDetailRes.builder()
                .id(order.getId())
                .orderNo(order.getOrderNumber())
                .userId(order.getUserId())
                .userNickname(user != null ? user.getNickname() : null)
                .userEmail(user != null ? user.getEmail() : null)
                .storeId(order.getStoreId())
                .storeName(store != null ? store.getStoreName() : null)
                .shippingStatus(order.getStatus())
                .shippingStatusName(OrderStatusEnum.getNameByCode(order.getStatus()))
                .totalItems(order.getTotalItems())
                .shippingMethod(order.getShippingMethod())
                .shippingMethodName(getShippingMethodLabel(order.getShippingMethod()))
                .recipientName(order.getRecipientName())
                .recipientPhone(order.getRecipientPhone())
                .recipientAddress(order.getRecipientAddress())
                .storeCode(order.getStoreCode())
                .storeName2(order.getStoreName())
                .storeAddress(order.getStoreAddress())
                .trackingNo(order.getTrackingNo())
                .remark(order.getRemark())
                .items(items.stream().map(this::convertItemToRes).collect(Collectors.toList()))
                .statusHistory(statusHistory)
                .subtotal(0L)
                .shippingFee(SHIPPING_FEE)
                .discount(0L)
                .totalAmount(SHIPPING_FEE)
                .paymentMethod("GOLD")
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .shippedAt(order.getShippedAt())
                .completedAt(order.getCompletedAt())
                .cancelledAt(order.getCancelledAt())
                .cancelledBy(order.getCancelledBy())
                .cancelReason(order.getCancelReason())
                .build();
    }

    // ─── US1: Update order status (unified state machine) ────────────────────

    @Override
    @Transactional
    public OrderRes updateOrderStatus(String orderId, UpdateOrderStatusReq req,
                                      String operatorId, String operatorType) {
        log.info("🔄 更新訂單狀態：orderId={}, targetStatus={}", orderId, req.getTargetStatus());

        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new BusinessException("NOT_FOUND", "訂單不存在");
        }

        OrderStatusEnum current = OrderStatusEnum.fromCode(order.getStatus());
        OrderStatusEnum target = OrderStatusEnum.fromCode(req.getTargetStatus());

        // Idempotency: same status is a no-op
        if (current == target) {
            log.info("ℹ️ 訂單狀態相同，無操作：{}", current);
            return convertToRes(order);
        }

        validateTransition(current, target);

        String fromStatus = order.getStatus();
        order.setStatus(target.getCode());
        order.setUpdatedAt(LocalDateTime.now());

        if (target == OrderStatusEnum.SHIPPED) {
            order.setShippedAt(LocalDateTime.now());
            if (isNotBlank(req.getTrackingNo())) {
                order.setTrackingNo(req.getTrackingNo());
            }
        } else if (target == OrderStatusEnum.COMPLETED) {
            order.setCompletedAt(LocalDateTime.now());
        }

        orderMapper.updateByPrimaryKey(order);
        insertStatusLog(orderId, fromStatus, target.getCode(), operatorId, operatorType, req.getRemark());

        log.info("✅ 訂單狀態更新：{} → {}", fromStatus, target.getCode());
        return convertToRes(order);
    }

    // ─── US1 / US3: Cancel order ──────────────────────────────────────────────

    @Override
    @Transactional
    public OrderRes cancelOrder(String orderId, OrderCancelReq req,
                                String operatorId, String operatorType) {
        log.info("🔍 取消訂單：orderId={}", orderId);

        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new BusinessException("NOT_FOUND", "訂單不存在");
        }

        OrderStatusEnum currentStatus = OrderStatusEnum.fromCode(order.getStatus());
        if (!currentStatus.isCancellable()) {
            throw new ConflictException("CANCEL_NOT_ALLOWED",
                    "訂單已出貨，無法取消（currentStatus=" + currentStatus.getCode() + "）");
        }

        String fromStatus = order.getStatus();
        String cancelReason = req != null ? req.getCancelReason() : null;

        order.setStatus(OrderStatusEnum.CANCELLED.getCode());
        order.setCancelledAt(LocalDateTime.now());
        order.setCancelledBy(operatorId);
        order.setCancelReason(cancelReason);
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateByPrimaryKey(order);

        // ✅ Restore prize boxes to IN_BOX
        OrderItemExample itemExample = new OrderItemExample();
        itemExample.createCriteria().andOrderIdEqualTo(orderId);
        List<OrderItem> items = orderItemMapper.selectByExample(itemExample);
        for (OrderItem item : items) {
            PrizeBox box = prizeBoxMapper.selectByPrimaryKey(item.getPrizeBoxId());
            if (box != null) {
                box.setStatus(PrizeBoxStatusEnum.IN_BOX.getCode());
                box.setOrderId(null);
                box.setShippedAt(null);
                prizeBoxMapper.updateByPrimaryKey(box);
            }
        }

        insertStatusLog(orderId, fromStatus, OrderStatusEnum.CANCELLED.getCode(),
                operatorId, operatorType,
                cancelReason != null ? "取消原因：" + cancelReason : null);

        log.info("✅ 訂單已取消");
        return convertToRes(order);
    }

    // ─── Legacy methods (backward compat) ─────────────────────────────────────

    @Override
    @Transactional
    public void prepareShipping(String orderId, String operatorId) {
        UpdateOrderStatusReq req = new UpdateOrderStatusReq();
        req.setTargetStatus(OrderStatusEnum.PREPARING.getCode());
        updateOrderStatus(orderId, req, operatorId, "STORE_OWNER");
    }

    @Override
    @Transactional
    public void ship(String orderId, OrderShipReq req, String operatorId) {
        UpdateOrderStatusReq updateReq = new UpdateOrderStatusReq();
        updateReq.setTargetStatus(OrderStatusEnum.SHIPPED.getCode());
        updateReq.setTrackingNo(req.getTrackingNo());
        updateReq.setRemark(req.getRemark());
        updateOrderStatus(orderId, updateReq, operatorId, "STORE_OWNER");
    }

    @Override
    @Transactional
    public void complete(String orderId, String operatorId) {
        UpdateOrderStatusReq req = new UpdateOrderStatusReq();
        req.setTargetStatus(OrderStatusEnum.COMPLETED.getCode());
        updateOrderStatus(orderId, req, operatorId, "STORE_OWNER");
    }

    @Override
    @Transactional
    public void cancel(String orderId, OrderCancelReq req, String operatorId) {
        cancelOrder(orderId, req, operatorId, "ADMIN");
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void validateTransition(OrderStatusEnum current, OrderStatusEnum target) {
        boolean valid = (current == OrderStatusEnum.PENDING && target == OrderStatusEnum.PREPARING)
                || (current == OrderStatusEnum.PREPARING && target == OrderStatusEnum.SHIPPED)
                || (current == OrderStatusEnum.SHIPPED && target == OrderStatusEnum.COMPLETED);

        if (!valid) {
            throw new ConflictException("INVALID_STATE_TRANSITION",
                    String.format("訂單狀態無法從 %s 轉換至 %s", current.getCode(), target.getCode()));
        }
    }

    private void insertStatusLog(String orderId, String fromStatus, String toStatus,
                                  String operatorId, String operatorType, String remark) {
        OrderStatusLog statusLog = new OrderStatusLog();
        statusLog.setId(UUID.randomUUID().toString());
        statusLog.setOrderId(orderId);
        statusLog.setFromStatus(fromStatus);
        statusLog.setToStatus(toStatus);
        statusLog.setOperatorId(operatorId);
        statusLog.setOperatorType(operatorType);
        statusLog.setRemark(remark);
        statusLog.setCreatedAt(LocalDateTime.now());
        orderStatusLogMapper.insert(statusLog);
    }

    private String generateOrderNumber() {
        String datePrefix = "ORD" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        OrderExample example = new OrderExample();
        example.createCriteria().andOrderNumberLike(datePrefix + "%");
        example.setOrderByClause("order_number DESC");
        List<Order> orders = orderMapper.selectByExample(example);
        int sequence = orders.isEmpty() ? 1
                : Integer.parseInt(orders.get(0).getOrderNumber().substring(datePrefix.length())) + 1;
        return datePrefix + String.format("%06d", sequence);
    }

    private OrderRes convertToRes(Order order) {
        User user = userMapper.selectByPrimaryKey(order.getUserId());
        Store store = storeMapper.selectByPrimaryKey(order.getStoreId());

        return OrderRes.builder()
                .id(order.getId())
                .orderNo(order.getOrderNumber())
                .userId(order.getUserId())
                .userNickname(user != null ? user.getNickname() : null)
                .userEmail(user != null ? user.getEmail() : null)
                .storeId(order.getStoreId())
                .storeName(store != null ? store.getStoreName() : null)
                .shippingStatus(order.getStatus())
                .shippingStatusName(OrderStatusEnum.getNameByCode(order.getStatus()))
                .totalItems(order.getTotalItems())
                .shippingMethod(order.getShippingMethod())
                .shippingMethodName(getShippingMethodLabel(order.getShippingMethod()))
                .recipientName(order.getRecipientName())
                .recipientPhone(order.getRecipientPhone())
                .trackingNo(order.getTrackingNo())
                .totalAmount(SHIPPING_FEE)
                .paymentMethod("GOLD")
                .createdAt(order.getCreatedAt())
                .shippedAt(order.getShippedAt())
                .completedAt(order.getCompletedAt())
                .build();
    }

    private OrderItemRes convertItemToRes(OrderItem item) {
        return OrderItemRes.builder()
                .id(item.getId())
                .orderId(item.getOrderId())
                .prizeBoxId(item.getPrizeBoxId())
                .lotteryId(item.getLotteryId())
                .lotteryTitle(item.getLotteryTitle())
                .lotteryImageUrl(item.getLotteryImageUrl())
                .prizeId(item.getPrizeId())
                .prizeName(item.getPrizeName())
                .prizeImageUrl(item.getPrizeImageUrl())
                .prizeLevel(item.getPrizeLevel())
                .createdAt(item.getCreatedAt())
                .build();
    }

    private StatusLogRes convertLogToRes(OrderStatusLog log) {
        return StatusLogRes.builder()
                .fromStatus(log.getFromStatus())
                .fromStatusLabel(log.getFromStatus() != null
                        ? OrderStatusEnum.getNameByCode(log.getFromStatus()) : null)
                .toStatus(log.getToStatus())
                .toStatusLabel(OrderStatusEnum.getNameByCode(log.getToStatus()))
                .operatorId(log.getOperatorId())
                .operatorType(log.getOperatorType())
                .remark(log.getRemark())
                .createdAt(log.getCreatedAt())
                .build();
    }

    private String getShippingMethodLabel(String code) {
        if (code == null) return null;
        switch (code) {
            case "HOME_DELIVERY": return "宅配到府";
            case "SEVEN_ELEVEN": return "7-ELEVEN 超商取貨";
            case "FAMILY_MART": return "全家超商取貨";
            default: return code;
        }
    }

    private boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }
}

