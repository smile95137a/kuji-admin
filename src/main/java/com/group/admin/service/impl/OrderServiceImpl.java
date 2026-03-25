package com.group.admin.service.impl;

import com.group.admin.condition.OrderCondition;
import com.group.admin.entity.*;
import com.group.admin.enums.OrderStatusEnum;
import com.group.admin.example.OrderExample;
import com.group.admin.example.OrderItemExample;
import com.group.admin.example.OrderStatusLogExample;
import com.group.admin.example.PrizeBoxExample;
import com.group.admin.example.StoreUserExample;
import com.group.admin.exception.BusinessException;
import com.group.admin.mapper.*;
import com.group.admin.repository.OrderRepository;
import com.group.admin.req.common.QueryReq;
import com.group.admin.req.order.CancelOrderReq;
import com.group.admin.req.order.CreateOrderReq;
import com.group.admin.req.order.OrderCancelReq;
import com.group.admin.req.order.OrderShipReq;
import com.group.admin.req.order.ShipInfoReq;
import com.group.admin.req.order.UpdateOrderStatusReq;
import com.group.admin.res.order.OrderDetailRes;
import com.group.admin.res.order.OrderItemRes;
import com.group.admin.res.order.OrderRes;
import com.group.admin.res.order.StatusLogRes;
import com.group.admin.service.OrderService;
import com.group.admin.service.ConsumptionRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 訂單服務實作
 *
 * @author Kuji Admin
 * @since 2026-01-09
 */
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
    private final StoreUserMapper storeUserMapper;
    private final OrderRepository orderRepository;
    private final ConsumptionRecordService consumptionRecordService;

    private static final Long SHIPPING_FEE = 60L;

    // ==================== 訂單建立 ====================

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
            order.setTrackingNo(null);
            order.setRemark(null);
            order.setCreatedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());

            orderMapper.insert(order);

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

                prizeBox.setOrderId(order.getId());
                prizeBoxMapper.updateByPrimaryKey(prizeBox);
            }

            recordStatusLog(order.getId(), null, OrderStatusEnum.PENDING.getCode(),
                    null, null, null);

            consumptionRecordService.recordConsumption(
                userId,
                "SHIPPING_FEE",
                null,
                null,
                order.getId(),
                order.getOrderNumber(),
                SHIPPING_FEE,
                0L,
                String.format("訂單運費：%s（配送方式：%s）", order.getOrderNumber(), order.getShippingMethod())
            );

            orderIds.add(order.getId());
        }

        log.info("✅ 訂單建立完成：orderCount={}，總運費={}元", orderIds.size(), orderIds.size() * SHIPPING_FEE);
        return orderIds;
    }

    @Override
    @Transactional
    public List<String> createOrdersFromPrizeBox(String userId, CreateOrderReq req) {
        log.info("🔍 從賞品盒建立訂單（DTO）：userId={}, prizeBoxCount={}", userId, req.getPrizeBoxIds().size());

        // 驗證賞品盒所有權與狀態
        List<PrizeBox> prizeBoxes = new ArrayList<>();
        for (String boxId : req.getPrizeBoxIds()) {
            PrizeBox box = prizeBoxMapper.selectByPrimaryKey(boxId);
            if (box == null) {
                throw new BusinessException("賞品盒不存在：" + boxId);
            }
            if (!userId.equals(box.getUserId())) {
                throw new BusinessException("賞品盒不屬於當前玩家：" + boxId);
            }
            if (!"IN_BOX".equals(box.getStatus())) {
                throw new BusinessException("賞品盒狀態不允許出貨：" + boxId);
            }
            prizeBoxes.add(box);
        }

        // 按店家分組
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
            order.setShippingMethod(req.getShippingMethod());
            order.setRecipientName(req.getRecipientName());
            order.setRecipientPhone(req.getRecipientPhone());
            order.setRecipientAddress(req.getRecipientAddress());
            order.setStoreCode(req.getStoreCode());
            order.setStoreName(req.getStoreName());
            order.setStoreAddress(req.getStoreAddress());
            order.setCreatedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());

            orderMapper.insert(order);

            List<OrderItem> items = new ArrayList<>();
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
                items.add(item);

                prizeBox.setOrderId(order.getId());
                prizeBox.setStatus("SHIPPING");
                prizeBoxMapper.updateByPrimaryKey(prizeBox);
            }

            if (!items.isEmpty()) {
                orderItemMapper.batchInsertOrderItems(items);
            }

            recordStatusLog(order.getId(), null, OrderStatusEnum.PENDING.getCode(),
                    userId, "USER", null);

            orderIds.add(order.getId());
        }

        log.info("✅ 訂單建立完成：orderCount={}", orderIds.size());
        return orderIds;
    }

    // ==================== 訂單查詢 ====================

    @Override
    public List<OrderRes> getOrders(QueryReq<OrderCondition> req) {
        OrderCondition condition = req != null ? req.getCondition() : null;
        List<Order> orders = orderMapper.selectByCondition(condition);
        return orders.stream().map(this::convertToRes).collect(Collectors.toList());
    }

    @Override
    public List<OrderRes> getOrderList(QueryReq<OrderCondition> req, String callerUserId, String callerRole) {
        if (req == null) {
            req = new QueryReq<>();
        }
        if (req.getCondition() == null) {
            req.setCondition(new OrderCondition());
        }

        OrderCondition condition = req.getCondition();

        // STORE_OWNER / STORE_EDITOR → 限定自己管理的店家
        if ("ROLE_STORE_OWNER".equals(callerRole) || "ROLE_STORE_EDITOR".equals(callerRole)) {
            String storeId = resolveStoreIdForUser(callerUserId);
            if (storeId == null) {
                log.warn("⚠️ 店家人員無關聯店家：userId={}", callerUserId);
                return Collections.emptyList();
            }
            condition.setStoreId(storeId);
        }

        List<Order> orders = orderMapper.selectByCondition(condition);
        long total = orderMapper.countByCondition(condition);
        log.info("🔍 查詢訂單列表：total={}", total);

        return orders.stream().map(this::convertToRes).collect(Collectors.toList());
    }

    @Override
    public OrderDetailRes getOrderDetail(String orderId) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new BusinessException("訂單不存在");
        }
        return buildOrderDetailRes(order);
    }

    @Override
    public OrderDetailRes getOrderById(String id, String callerUserId, String callerRole) {
        Order order = orderMapper.selectByPrimaryKey(id);
        if (order == null) {
            throw new BusinessException("ORDER_NOT_FOUND", "訂單不存在");
        }

        if ("ROLE_STORE_OWNER".equals(callerRole) || "ROLE_STORE_EDITOR".equals(callerRole)) {
            String storeId = resolveStoreIdForUser(callerUserId);
            if (storeId == null || !storeId.equals(order.getStoreId())) {
                throw new BusinessException("FORBIDDEN", "無權查看此訂單");
            }
        }

        return buildOrderDetailRes(order);
    }

    @Override
    public List<OrderRes> getPlayerOrderList(QueryReq<OrderCondition> req, String playerId) {
        if (req == null) {
            req = new QueryReq<>();
        }
        if (req.getCondition() == null) {
            req.setCondition(new OrderCondition());
        }
        req.getCondition().setUserId(playerId);

        List<Order> orders = orderMapper.selectByCondition(req.getCondition());
        return orders.stream().map(this::convertToRes).collect(Collectors.toList());
    }

    @Override
    public OrderDetailRes getPlayerOrderById(String orderId, String playerId) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new BusinessException("ORDER_NOT_FOUND", "訂單不存在");
        }
        if (!playerId.equals(order.getUserId())) {
            throw new BusinessException("FORBIDDEN", "無權查看此訂單");
        }
        return buildOrderDetailRes(order);
    }

    // ==================== 狀態變更 ====================

    @Override
    @Transactional
    public void prepareShipping(String orderId, String operatorId) {
        log.info("🔍 準備出貨：orderId={}", orderId);

        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new BusinessException("訂單不存在");
        }

        OrderStatusEnum currentStatus = OrderStatusEnum.fromCode(order.getStatus());
        if (currentStatus != OrderStatusEnum.PENDING) {
            throw new BusinessException("訂單狀態不允許此操作");
        }

        String fromStatus = order.getStatus();
        order.setStatus(OrderStatusEnum.PREPARING.getCode());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateByPrimaryKeySelective(order);

        recordStatusLog(orderId, fromStatus, OrderStatusEnum.PREPARING.getCode(),
                operatorId, "ADMIN", null);

        log.info("✅ 訂單狀態更新：PREPARING");
    }

    @Override
    @Transactional
    public void ship(String orderId, OrderShipReq req, String operatorId) {
        log.info("🔍 訂單出貨：orderId={}, trackingNo={}", orderId, req.getTrackingNo());

        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new BusinessException("訂單不存在");
        }

        OrderStatusEnum currentStatus = OrderStatusEnum.fromCode(order.getStatus());
        if (currentStatus != OrderStatusEnum.PREPARING) {
            throw new BusinessException("訂單狀態不允許此操作");
        }

        String fromStatus = order.getStatus();
        order.setStatus(OrderStatusEnum.SHIPPED.getCode());
        order.setTrackingNo(req.getTrackingNo());
        order.setRemark(req.getRemark());
        order.setShippedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateByPrimaryKeySelective(order);

        recordStatusLog(orderId, fromStatus, OrderStatusEnum.SHIPPED.getCode(),
                operatorId, "ADMIN", "物流單號：" + req.getTrackingNo());

        log.info("✅ 訂單已出貨");
    }

    @Override
    @Transactional
    public void complete(String orderId, String operatorId) {
        log.info("🔍 完成訂單：orderId={}", orderId);

        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new BusinessException("訂單不存在");
        }

        OrderStatusEnum currentStatus = OrderStatusEnum.fromCode(order.getStatus());
        if (currentStatus != OrderStatusEnum.SHIPPED) {
            throw new BusinessException("訂單狀態不允許此操作");
        }

        String fromStatus = order.getStatus();
        order.setStatus(OrderStatusEnum.COMPLETED.getCode());
        order.setCompletedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateByPrimaryKeySelective(order);

        recordStatusLog(orderId, fromStatus, OrderStatusEnum.COMPLETED.getCode(),
                operatorId, operatorId != null ? "ADMIN" : "SYSTEM", null);

        log.info("✅ 訂單已完成");
    }

    @Override
    @Transactional
    public void cancel(String orderId, OrderCancelReq req, String operatorId) {
        log.info("🔍 取消訂單：orderId={}", orderId);

        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new BusinessException("訂單不存在");
        }

        OrderStatusEnum currentStatus = OrderStatusEnum.fromCode(order.getStatus());
        if (!currentStatus.isCancellable()) {
            throw new BusinessException("訂單狀態不允許取消");
        }

        String fromStatus = order.getStatus();
        order.setStatus(OrderStatusEnum.CANCELLED.getCode());
        order.setCancelReason(req.getReason());
        order.setCancelledBy(operatorId);
        order.setCancelledAt(LocalDateTime.now());
        order.setRemark(req.getReason());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateByPrimaryKeySelective(order);

        restorePrizeBoxes(orderId);

        recordStatusLog(orderId, fromStatus, OrderStatusEnum.CANCELLED.getCode(),
                operatorId, "ADMIN", "取消原因：" + req.getReason());

        log.info("✅ 訂單已取消");
    }

    @Override
    @Transactional
    public void updateOrderStatus(String id, UpdateOrderStatusReq req, String operatorId, String operatorType) {
        log.info("🔍 統一狀態更新：orderId={}, targetStatus={}", id, req.getTargetStatus());

        Order order = orderMapper.selectByPrimaryKey(id);
        if (order == null) {
            throw new BusinessException("ORDER_NOT_FOUND", "訂單不存在");
        }

        OrderStatusEnum currentStatus = OrderStatusEnum.fromCode(order.getStatus());
        OrderStatusEnum targetStatus = OrderStatusEnum.fromCode(req.getTargetStatus());

        validateTransition(currentStatus, targetStatus);

        String fromStatus = order.getStatus();
        order.setStatus(targetStatus.getCode());
        order.setUpdatedAt(LocalDateTime.now());

        switch (targetStatus) {
            case SHIPPED:
                if (isNotBlank(req.getTrackingNo())) {
                    order.setTrackingNo(req.getTrackingNo());
                }
                order.setShippedAt(LocalDateTime.now());
                break;
            case COMPLETED:
                order.setCompletedAt(LocalDateTime.now());
                break;
            default:
                break;
        }

        if (isNotBlank(req.getRemark())) {
            order.setRemark(req.getRemark());
        }

        orderMapper.updateByPrimaryKeySelective(order);

        String logRemark = req.getRemark();
        if (targetStatus == OrderStatusEnum.SHIPPED && isNotBlank(req.getTrackingNo())) {
            logRemark = "物流單號：" + req.getTrackingNo()
                    + (isNotBlank(req.getRemark()) ? "；" + req.getRemark() : "");
        }

        recordStatusLog(id, fromStatus, targetStatus.getCode(),
                operatorId, operatorType, logRemark);

        log.info("✅ 訂單狀態更新：{} → {}", fromStatus, targetStatus.getCode());
    }

    @Override
    @Transactional
    public void cancelOrder(String id, CancelOrderReq req, String operatorId, String operatorType) {
        log.info("🔍 取消訂單：orderId={}, operator={}", id, operatorId);

        Order order = orderMapper.selectByPrimaryKey(id);
        if (order == null) {
            throw new BusinessException("ORDER_NOT_FOUND", "訂單不存在");
        }

        OrderStatusEnum currentStatus = OrderStatusEnum.fromCode(order.getStatus());
        if (!currentStatus.isCancellable()) {
            throw new BusinessException("訂單狀態不允許取消（目前狀態：" + currentStatus.getName() + "）");
        }

        String fromStatus = order.getStatus();
        String cancelReason = req != null ? req.getCancelReason() : null;

        order.setStatus(OrderStatusEnum.CANCELLED.getCode());
        order.setCancelReason(cancelReason);
        order.setCancelledBy(operatorId);
        order.setCancelledAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateByPrimaryKeySelective(order);

        restorePrizeBoxes(id);

        recordStatusLog(id, fromStatus, OrderStatusEnum.CANCELLED.getCode(),
                operatorId, operatorType,
                cancelReason != null ? "取消原因：" + cancelReason : null);

        log.info("✅ 訂單已取消，獎品已歸還");
    }

    @Override
    @Transactional
    public void submitShippingInfo(String orderId, ShipInfoReq req, String userId) {
        log.info("📦 提交出貨資訊：orderId={}, userId={}", orderId, userId);

        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new BusinessException("訂單不存在");
        }

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("無權限操作此訂單");
        }

        OrderStatusEnum currentStatus = OrderStatusEnum.fromCode(order.getStatus());
        if (currentStatus != OrderStatusEnum.PENDING) {
            throw new BusinessException("訂單已確認，無法修改出貨資訊");
        }

        String method = req.getShippingMethod();
        if ("HOME_DELIVERY".equals(method)) {
            if (isBlank(req.getRecipientName())) {
                throw new BusinessException("宅配需填入收件人姓名");
            }
            if (isBlank(req.getRecipientPhone())) {
                throw new BusinessException("宅配需填入收件人電話");
            }
            if (isBlank(req.getRecipientAddress())) {
                throw new BusinessException("宅配需填入收件地址");
            }
        } else if ("SEVEN_ELEVEN".equals(method) || "FAMILY_MART".equals(method)) {
            if (isBlank(req.getStoreCode())) {
                throw new BusinessException("超商取貨需填入分店代碼");
            }
            if (isBlank(req.getStoreName())) {
                throw new BusinessException("超商取貨需填入分店名稱");
            }
        } else {
            throw new BusinessException("不支援的配送方式");
        }

        order.setShippingMethod(method);
        order.setRecipientName(req.getRecipientName());
        order.setRecipientPhone(req.getRecipientPhone());
        order.setRecipientAddress(req.getRecipientAddress());
        order.setStoreCode(req.getStoreCode());
        order.setStoreName(req.getStoreName());
        order.setStoreAddress(req.getStoreAddress());
        order.setRemark(req.getRemark());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateByPrimaryKey(order);

        log.info("✅ 出貨資訊已更新");
    }

    // ==================== 內部輔助方法 ====================

    private void validateTransition(OrderStatusEnum from, OrderStatusEnum to) {
        boolean valid = switch (to) {
            case PREPARING -> from == OrderStatusEnum.PENDING;
            case SHIPPED -> from == OrderStatusEnum.PREPARING;
            case COMPLETED -> from == OrderStatusEnum.SHIPPED;
            case CANCELLED -> from.isCancellable();
            default -> false;
        };
        if (!valid) {
            throw new BusinessException(
                    String.format("不允許的狀態轉換：%s → %s", from.getName(), to.getName()));
        }
    }

    private void restorePrizeBoxes(String orderId) {
        OrderItemExample itemExample = new OrderItemExample();
        itemExample.createCriteria().andOrderIdEqualTo(orderId);
        List<OrderItem> items = orderItemMapper.selectByExample(itemExample);

        for (OrderItem item : items) {
            if (item.getPrizeBoxId() != null) {
                PrizeBox prizeBox = prizeBoxMapper.selectByPrimaryKey(item.getPrizeBoxId());
                if (prizeBox != null) {
                    prizeBox.setStatus("IN_BOX");
                    prizeBox.setOrderId(null);
                    prizeBox.setShippedAt(null);
                    prizeBoxMapper.updateByPrimaryKey(prizeBox);
                    log.info("🔄 獎品已歸還賞品盒：prizeBoxId={}", prizeBox.getId());
                }
            }
        }
    }

    private String resolveStoreIdForUser(String adminUserId) {
        StoreUserExample example = new StoreUserExample();
        example.createCriteria().andAdminUserIdEqualTo(adminUserId);
        List<StoreUser> storeUsers = storeUserMapper.selectByExample(example);
        if (storeUsers.isEmpty()) {
            return null;
        }
        return storeUsers.get(0).getStoreId();
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }

    private String generateOrderNumber() {
        String datePrefix = "ORD" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        OrderExample example = new OrderExample();
        example.createCriteria().andOrderNumberLike(datePrefix + "%");
        example.setOrderByClause("order_number DESC");

        List<Order> orders = orderMapper.selectByExample(example);

        int sequence = 1;
        if (!orders.isEmpty()) {
            String lastOrderNumber = orders.get(0).getOrderNumber();
            String lastSequence = lastOrderNumber.substring(datePrefix.length());
            sequence = Integer.parseInt(lastSequence) + 1;
        }

        return datePrefix + String.format("%06d", sequence);
    }

    private void recordStatusLog(String orderId, String fromStatus, String toStatus,
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

    // ==================== DTO 轉換 ====================

    private OrderDetailRes buildOrderDetailRes(Order order) {
        OrderItemExample itemExample = new OrderItemExample();
        itemExample.createCriteria().andOrderIdEqualTo(order.getId());
        List<OrderItem> items = orderItemMapper.selectByExample(itemExample);

        OrderStatusLogExample logExample = new OrderStatusLogExample();
        logExample.createCriteria().andOrderIdEqualTo(order.getId());
        logExample.setOrderByClause("created_at ASC");
        List<OrderStatusLog> logs = orderStatusLogMapper.selectByExample(logExample);

        User user = userMapper.selectByPrimaryKey(order.getUserId());
        Store store = storeMapper.selectByPrimaryKey(order.getStoreId());

        return OrderDetailRes.builder()
                .id(order.getId())
                .orderNo(order.getOrderNumber())
                .userId(order.getUserId())
                .userNickname(user != null ? user.getNickname() : null)
                .userEmail(user != null ? user.getEmail() : null)
                .storeId(order.getStoreId())
                .storeName(store != null ? store.getStoreName() : null)
                .storeLogoUrl(store != null ? store.getLogoUrl() : null)
                .shippingStatus(order.getStatus())
                .shippingStatusName(OrderStatusEnum.getNameByCode(order.getStatus()))
                .totalItems(order.getTotalItems())
                .shippingMethod(order.getShippingMethod())
                .shippingMethodName(getShippingMethodLabel(order.getShippingMethod()))
                .recipientName(order.getRecipientName())
                .recipientPhone(order.getRecipientPhone())
                .recipientAddress(order.getRecipientAddress())
                .storeCode(order.getStoreCode())
                .storeAddress(order.getStoreAddress())
                .trackingNo(order.getTrackingNo())
                .remark(order.getRemark())
                .items(items.stream().map(this::convertItemToRes).collect(Collectors.toList()))
                .statusHistory(logs.stream().map(this::convertLogToRes).collect(Collectors.toList()))
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
                .fromStatusLabel(log.getFromStatus() != null ? OrderStatusEnum.getNameByCode(log.getFromStatus()) : null)
                .toStatus(log.getToStatus())
                .toStatusLabel(OrderStatusEnum.getNameByCode(log.getToStatus()))
                .operatorId(log.getOperatorId())
                .operatorType(log.getOperatorType())
                .remark(log.getRemark())
                .createdAt(log.getCreatedAt())
                .build();
    }

    private String getShippingMethodLabel(String method) {
        if (method == null) return null;
        return switch (method) {
            case "HOME_DELIVERY" -> "宅配";
            case "SEVEN_ELEVEN" -> "7-11取貨";
            case "FAMILY_MART" -> "全家取貨";
            default -> method;
        };
    }
}